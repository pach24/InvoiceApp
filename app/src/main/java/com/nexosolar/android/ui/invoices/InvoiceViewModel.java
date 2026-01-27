

package com.nexosolar.android.ui.invoices;

import android.os.Handler;
import android.os.Looper;
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
    private boolean filtrosInicializados = false;
    private final GetInvoicesUseCase getInvoicesUseCase;
    private final FilterInvoicesUseCase filterInvoicesUseCase;
    private List<Invoice> facturasOriginales = new ArrayList<>();

    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> showEmptyError = new MutableLiveData<>(false);
    private final MutableLiveData<ErrorType> errorTypeState = new MutableLiveData<>(ErrorType.NONE);
    private final MutableLiveData<InvoiceFilters> filtrosActuales = new MutableLiveData<>(new InvoiceFilters());
    private final MutableLiveData<String> errorValidacion = new MutableLiveData<>();

    // Referencia del Handler para poder cancelarlo
    private Handler loadingHandler;
    private static final long LOADING_DELAY_MS = 40;  // CORREGIDO: Era 5000 (5 segundos)

    // Flag para diferenciar primera carga vs recargas
    private boolean isFirstLoad = true;

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
     * Carga las facturas de manera optimizada.
     * 1. Evita el parpadeo del shimmer si ya hay datos o si la respuesta es muy rápida.
     * 2. Utiliza postValue para garantizar la seguridad entre hilos (Thread Safety).
     * 3. Mantiene la arquitectura Clean al no depender de LiveData en el UseCase.
     */
    public void cargarFacturas() {
        Log.d(TAG, "cargarFacturas() - isFirstLoad: " + isFirstLoad + ", hayCaché: " + !facturasOriginales.isEmpty());

        // 1) Resetear flags de error/vacío ANTES de que la Activity recalcule estado
        showEmptyError.setValue(false);
        errorTypeState.setValue(ErrorType.NONE);

        // 2) Cancelar cualquier Handler pendiente
        if (loadingHandler != null) {
            loadingHandler.removeCallbacksAndMessages(null);
            loadingHandler = null;
        }

        boolean hayCache = facturasOriginales != null && !facturasOriginales.isEmpty();

        // 3) Gestionar isLoading según caso
        if (!hayCache) {
            // No hay datos: siempre shimmer inmediato (tanto primera carga como recarga desde error)
            isLoading.setValue(true);
        } else {
            // Hay datos en memoria: no mostrar shimmer, mantenemos la lista
            isLoading.setValue(false);
        }

        // 4) Llamada al repositorio
        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                // Cancelar handler por seguridad (aunque no lo usamos ya para delay)
                if (loadingHandler != null) {
                    loadingHandler.removeCallbacksAndMessages(null);
                    loadingHandler = null;
                }

                isFirstLoad = false;

                if (result != null && !result.isEmpty()) {
                    facturasOriginales = result;
                    facturas.postValue(result);
                    errorTypeState.postValue(ErrorType.NONE);
                    showEmptyError.postValue(false);
                } else {
                    // Respuesta vacía
                    if (facturasOriginales == null || facturasOriginales.isEmpty()) {
                        facturas.postValue(new ArrayList<>());
                    }
                    showEmptyError.postValue(false);
                }

                isLoading.postValue(false);
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "Error cargando facturas: " + error.getMessage());

                if (loadingHandler != null) {
                    loadingHandler.removeCallbacksAndMessages(null);
                    loadingHandler = null;
                }

                isFirstLoad = false;

                ErrorType tipoDetectado;
                if (error instanceof java.net.UnknownHostException) {
                    tipoDetectado = ErrorType.NETWORK;
                } else if (error instanceof java.net.SocketTimeoutException) {
                    tipoDetectado = ErrorType.SERVER_GENERIC;
                } else if (error instanceof java.io.IOException) {
                    tipoDetectado = ErrorType.NETWORK;
                } else {
                    tipoDetectado = ErrorType.SERVER_GENERIC;
                }

                boolean hayCacheLocal = facturasOriginales != null && !facturasOriginales.isEmpty();

                if (hayCacheLocal) {
                    // Hay caché: mantenemos datos y no mostramos pantalla de error
                    facturas.postValue(facturasOriginales);
                    errorTypeState.postValue(ErrorType.NONE);
                    showEmptyError.postValue(false);
                    isLoading.postValue(false);  // ← AQUÍ el postValue normal
                } else {
                    // Sin caché: MOSTRAR SHIMMER X ms ANTES DE MOSTRAR ERROR
                    // NO ponemos isLoading a false aquí, lo mantiene en true el delay lo hace
                    mostrarErrorConDelay(tipoDetectado, 1500);
                }
            }

            // Agrega este método al final de la clase
            private void mostrarErrorConDelay(ErrorType errorType, long delayMs) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    isLoading.postValue(false);  // ← Ahora sí, después del delay
                    errorTypeState.postValue(errorType);
                    showEmptyError.postValue(true);
                }, delayMs);
            }


        });
    }

    // En InvoiceViewModel.java

    private void mostrarErrorConDelay(ErrorType errorType, long delayMs) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isLoading.postValue(false);
            showEmptyError.postValue(true);
            errorTypeState.postValue(errorType);
        }, delayMs);
    }




    // ===== Gestión de filtros =====

    /**
     * Inicializa los filtros con valores predeterminados basados en los datos cargados.
     * Solo se ejecuta si no hay filtros ya establecidos.
     */
    public void inicializarFiltros() {
        // Solo inicializar si es la primera vez
        if (filtrosInicializados) {
            return;
        }

        // Si ya hay filtros válidos (doble chequeo defensivo), tampoco reinicializar
        if (filtrosActuales.getValue() != null &&
                !filtrosActuales.getValue().getFilteredStates().isEmpty()) {
            filtrosInicializados = true;
            return;
        }

        InvoiceFilters filtros = new InvoiceFilters();
        filtros.setStartDate(null);
        filtros.setEndDate(null);
        filtros.setMinAmount(0.0);
        filtros.setMaxAmount((double) getMaxImporte());
        filtros.setFilteredStates(new ArrayList<>());
        filtrosActuales.postValue(filtros);
        filtrosInicializados = true;
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
        nuevosFiltros.setStartDate(null);
        nuevosFiltros.setEndDate(null);
        nuevosFiltros.setMinAmount(0.0);
        nuevosFiltros.setMaxAmount((double) getMaxImporte());
        nuevosFiltros.setFilteredStates(new ArrayList<>());
        filtrosActuales.setValue(nuevosFiltros);
        filtrosInicializados = false;
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
                filtros.getFilteredStates(),
                filtros.getStartDate(),
                filtros.getEndDate(),
                filtros.getMinAmount(),
                filtros.getMaxAmount()
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

    // ===== Lifecycle Management =====

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar Handler al destruir el ViewModel
        if (loadingHandler != null) {
            loadingHandler.removeCallbacksAndMessages(null);
            loadingHandler = null;
        }
    }
}