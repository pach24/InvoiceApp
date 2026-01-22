package com.nexosolar.android.ui.invoices;

import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * InvoiceViewModel
 *
 * ViewModel que gestiona el estado de la lista de facturas y filtros aplicados.
 * Coordina la carga de datos desde el repositorio, maneja estados de error diferenciados
 * (red vs servidor), y ejecuta filtros sobre los datos originales sin modificarlos.
 *
 * Estados gestionados:
 * - isLoading: indica si hay una operación de carga en curso (shimmer)
 * - errorTypeState: clasifica errores en NETWORK, SERVER_GENERIC o NONE
 * - facturas: lista visible en la UI (puede ser filtrada)
 * - facturasOriginales: copia inmutable de los datos originales
 * - filtrosActuales: estado actual de los filtros aplicados
 */
public class InvoiceViewModel extends ViewModel {

    private static final String TAG = "InvoiceViewModel";

    // ===== Enumeración de tipos de error =====

    public enum ErrorType {
        NONE,
        NETWORK,
        SERVER_GENERIC
    }

    // ===== Variables de instancia =====

    private final GetInvoicesUseCase getInvoicesUseCase;
    private final FilterInvoicesUseCase filterInvoicesUseCase;

    private List<Invoice> facturasOriginales = new ArrayList<>();

    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> showEmptyError = new MutableLiveData<>(false);
    private final MutableLiveData<ErrorType> errorTypeState = new MutableLiveData<>(ErrorType.NONE);
    private final MutableLiveData<InvoiceFilters> filtrosActuales = new MutableLiveData<>(new InvoiceFilters());
    private final MutableLiveData<String> errorValidacion = new MutableLiveData<>();

    // ===== Constructor =====

    public InvoiceViewModel(GetInvoicesUseCase getInvoicesUseCase, FilterInvoicesUseCase filterInvoicesUseCase) {
        this.getInvoicesUseCase = getInvoicesUseCase;
        this.filterInvoicesUseCase = filterInvoicesUseCase;
        cargarFacturas();
    }

    // ===== Getters de LiveData =====

    public LiveData<List<Invoice>> getFacturas() {
        return facturas;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getShowEmptyError() {
        return showEmptyError;
    }

    public LiveData<ErrorType> getErrorTypeState() {
        return errorTypeState;
    }

    public LiveData<InvoiceFilters> getFiltrosActuales() {
        return filtrosActuales;
    }

    public LiveData<String> getErrorValidacion() {
        return errorValidacion;
    }

    // ===== Métodos públicos de carga =====

    /**
     * Carga las facturas desde el repositorio.
     * Gestiona estados de carga, éxito y error de manera diferenciada.
     */
    /**
     * Carga las facturas de manera optimizada.
     * 1. Evita el parpadeo del shimmer si ya hay datos o si la respuesta es muy rápida.
     * 2. Utiliza postValue para garantizar la seguridad entre hilos (Thread Safety).
     * 3. Mantiene la arquitectura Clean al no depender de LiveData en el UseCase.
     */
    public void cargarFacturas() {
        errorTypeState.setValue(ErrorType.NONE);

        if (facturasOriginales != null && !facturasOriginales.isEmpty()) {
            // Si ya tenemos datos en memoria, no mostramos loading
            isLoading.setValue(false);
        } else {
            // Si no hay datos, esperamos un breve instante (40ms) antes de mostrar el shimmer.
            // Si los datos llegan antes de ese tiempo (desde Room), el shimmer nunca se verá, en cambio, si viene de red, es probable que tarde algo más y por lo tanto se mostrará
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (facturasOriginales == null || facturasOriginales.isEmpty()) {
                    isLoading.postValue(true);
                }
            }, 40);
        }

        // LLAMADA AL USE CASE
        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                // Importante: Usamos postValue porque el callback viene de un hilo secundario del Repositorio
                if (result != null && !result.isEmpty()) {
                    facturasOriginales = result;
                    facturas.postValue(result);
                    errorTypeState.postValue(ErrorType.NONE);
                } else {
                    // Si la respuesta es vacía pero ya teníamos datos previos, mantenemos los previos
                    if (facturasOriginales == null || facturasOriginales.isEmpty()) {
                        facturas.postValue(new ArrayList<>());
                    }
                }

                // Finalizamos el estado de carga
                isLoading.postValue(false);
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error cargando facturas: " + error.getMessage());

                // Identificamos el tipo de error
                ErrorType tipoDetectado = esErrorDeRed(error)
                        ? ErrorType.NETWORK
                        : ErrorType.SERVER_GENERIC;

                // Si ya tenemos datos (caché), no mostramos pantalla de error, solo ocultamos loading
                if (facturasOriginales != null && !facturasOriginales.isEmpty()) {
                    facturas.postValue(facturasOriginales);
                    errorTypeState.postValue(ErrorType.NONE);
                } else {
                    // Si no hay datos y hay error, mostramos el error correspondiente
                    facturas.postValue(new ArrayList<>());
                    errorTypeState.postValue(tipoDetectado);
                }

                isLoading.postValue(false);
            }
        });
    }


    // ===== Gestión de filtros =====

    /**
     * Inicializa los filtros con valores predeterminados basados en los datos cargados.
     * Solo se ejecuta si no hay filtros ya establecidos.
     */
    public void inicializarFiltros() {
        if (filtrosActuales.getValue() != null &&
                !filtrosActuales.getValue().getEstadosSeleccionados().isEmpty()) {
            return;
        }

        InvoiceFilters filtros = new InvoiceFilters();
        filtros.setFechaInicio(null);
        filtros.setFechaFin(null);
        filtros.setImporteMin(0.0);
        filtros.setImporteMax((double) getMaxImporte());
        filtros.setEstadosSeleccionados(new ArrayList<>());

        filtrosActuales.postValue(filtros);
    }

    /**
     * Actualiza los filtros y los aplica a los datos originales.
     * Valida que las fechas sean coherentes antes de aplicar.
     */
    public void actualizarFiltros(InvoiceFilters filtros) {
        if (filtros.isValid()) {
            filtrosActuales.setValue(filtros);
            aplicarFiltros();
            errorValidacion.setValue(null);
        } else {
            errorValidacion.setValue("La fecha de inicio no puede ser posterior a la final.");
        }
    }

    /**
     * Actualiza el estado de los filtros sin aplicarlos de inmediato.
     * Útil para sincronizar UI con el estado del ViewModel.
     */
    public void actualizarEstadoFiltros(InvoiceFilters filtros) {
        this.filtrosActuales.setValue(filtros);
    }

    /**
     * Resetea los filtros a valores por defecto (sin restricciones).
     * Restaura la lista completa de facturas originales.
     */
    public void resetearFiltros() {
        InvoiceFilters nuevosFiltros = new InvoiceFilters();
        nuevosFiltros.setFechaInicio(null);
        nuevosFiltros.setFechaFin(null);
        nuevosFiltros.setImporteMin(0.0);
        nuevosFiltros.setImporteMax((double) getMaxImporte());
        nuevosFiltros.setEstadosSeleccionados(new ArrayList<>());

        filtrosActuales.setValue(nuevosFiltros);

        if (facturasOriginales != null) {
            facturas.setValue(new ArrayList<>(facturasOriginales));
        }
    }

    /**
     * Aplica los filtros actuales sobre los datos originales.
     * Ejecuta el filtrado en segundo plano para evitar bloquear la UI.
     */
    private void aplicarFiltros() {
        InvoiceFilters filtros = filtrosActuales.getValue();
        if (filtros == null) return;

        filtrarFacturas(
                filtros.getEstadosSeleccionados(),
                filtros.getFechaInicio(),
                filtros.getFechaFin(),
                filtros.getImporteMin(),
                filtros.getImporteMax()
        );
    }

    /**
     * Filtra las facturas según los criterios especificados.
     * La operación se ejecuta en hilo secundario para mantener la fluidez de la UI.
     */
    private void filtrarFacturas(List<String> estadosSeleccionados,
                                 LocalDate fechaInicio,
                                 LocalDate fechaFin,
                                 Double importeMin,
                                 Double importeMax) {
        isLoading.setValue(true);

        new Thread(() -> {
            List<Invoice> listaBase = (facturasOriginales != null)
                    ? facturasOriginales
                    : new ArrayList<>();

            List<Invoice> facturasFiltradas = filterInvoicesUseCase.execute(
                    listaBase, estadosSeleccionados, fechaInicio, fechaFin, importeMin, importeMax
            );

            facturas.postValue(facturasFiltradas);
            isLoading.postValue(false);
        }).start();
    }

    // ===== Métodos auxiliares =====

    /**
     * Determina si un error es de tipo red (sin conexión, timeout, etc.).
     */
    private boolean esErrorDeRed(Throwable t) {
        return t instanceof java.net.UnknownHostException ||
                t instanceof java.net.ConnectException ||
                t instanceof java.net.SocketTimeoutException;
    }

    /**
     * Retorna el importe máximo de todas las facturas originales.
     * Útil para configurar el límite superior del slider de filtros.
     */
    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return 0f;

        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            if (factura.getInvoiceAmount() > maxImporte) {
                maxImporte = factura.getInvoiceAmount();
            }
        }
        return maxImporte;
    }

    /**
     * Retorna la fecha más antigua de todas las facturas originales.
     */
    public LocalDate getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return null;

        LocalDate oldestDate = null;
        for (Invoice factura : facturasOriginales) {
            LocalDate currentDate = factura.getInvoiceDate();
            if (currentDate != null) {
                if (oldestDate == null || currentDate.isBefore(oldestDate)) {
                    oldestDate = currentDate;
                }
            }
        }
        return oldestDate;
    }

    /**
     * Retorna la fecha más reciente de todas las facturas originales.
     */
    public LocalDate getNewestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return null;

        LocalDate newest = null;
        for (Invoice factura : facturasOriginales) {
            if (factura.getInvoiceDate() != null) {
                if (newest == null || factura.getInvoiceDate().isAfter(newest)) {
                    newest = factura.getInvoiceDate();
                }
            }
        }
        return newest;
    }

    /**
     * Indica si hay datos cargados en el ViewModel.
     */
    public boolean hayDatosCargados() {
        return facturasOriginales != null && !facturasOriginales.isEmpty();
    }

    // ===== Métodos de testing =====

    @VisibleForTesting
    public void setFacturasOriginalesTest(List<Invoice> facturas) {
        this.facturasOriginales = facturas;
        this.facturas.setValue(facturas);
    }
}
