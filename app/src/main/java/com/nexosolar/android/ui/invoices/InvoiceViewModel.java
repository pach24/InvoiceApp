package com.nexosolar.android.ui.invoices;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.RepositoryCallback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceViewModel extends ViewModel {

    // LiveData para la lista de facturas que observa la UI
    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();

    // Nuevo LiveData para mostrar error vacío
    private final MutableLiveData<Boolean> showEmptyError = new MutableLiveData<>(false);
    public LiveData<Boolean> getShowEmptyError() { return showEmptyError; }

    private List<Invoice> facturasOriginales = new ArrayList<>();

    private final GetInvoicesUseCase getInvoicesUseCase;
    private final FilterInvoicesUseCase filterInvoicesUseCase;
    private boolean isFirstLoad = true;

    // --- LIVE DATA ---
    private final MutableLiveData<InvoiceFilters> filtrosActuales = new MutableLiveData<>(new InvoiceFilters());
    private final MutableLiveData<String> errorValidacion = new MutableLiveData<>();
    // Inicializamos en TRUE para que la primera carga ya muestre shimmer
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public InvoiceViewModel(GetInvoicesUseCase getInvoicesUseCase, FilterInvoicesUseCase filterInvoicesUseCase) {
        this.getInvoicesUseCase = getInvoicesUseCase;
        this.filterInvoicesUseCase = filterInvoicesUseCase;
        cargarFacturas();
    }

    public void cargarFacturas() {
        isLoading.setValue(true);
        showEmptyError.setValue(false); // Reseteamos error al empezar

        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                isLoading.postValue(false);
                if (result != null && !result.isEmpty()) {
                    facturasOriginales = result;
                    facturas.postValue(result);
                    // Si recargamos y ahora sí hay datos, quitamos el error
                    showEmptyError.postValue(false);
                } else {
                    // Si viene vacío y tampoco teníamos datos viejos...
                    if (facturasOriginales == null || facturasOriginales.isEmpty()) {
                        showEmptyError.postValue(true);
                    }
                    facturas.postValue(new ArrayList<>());
                }
            }


            @Override
            public void onError(Throwable error) {
                // Forzamos esperar 1s para que el usuario vea que "pensó"
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    error.printStackTrace();

                    // 1. PRIMERO determinamos qué mostrar (Datos viejos o Error)
                    if (facturasOriginales != null && !facturasOriginales.isEmpty()) {
                        // Si hay datos viejos, los mostramos y ocultamos el error
                        facturas.setValue(facturasOriginales); // Usa setValue ya que estamos en MainLooper
                        showEmptyError.setValue(false);
                    } else {
                        // Si no hay nada, mostramos lista vacía y ACTIVAMOS el error
                        facturas.setValue(new ArrayList<>());
                        showEmptyError.setValue(true);
                    }

                    // 2. POR ÚLTIMO apagamos el loading
                    // Al hacerlo al final, la UI ya tendrá el valor correcto de 'showEmptyError'
                    // cuando 'isLoading' pase a false.
                    isLoading.setValue(false);

                }, 1000);
            }
        });
    }


    public LiveData<List<Invoice>> getFacturas() { return facturas; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<InvoiceFilters> getFiltrosActuales() { return filtrosActuales; }
    public LiveData<String> getErrorValidacion() { return errorValidacion; }

    public void forzarRecarga() {
        isLoading.setValue(true); // Activar shimmer al recargar
        getInvoicesUseCase.refresh(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (Boolean.TRUE.equals(success)) {
                    cargarFacturas(); // cargarFacturas gestionará el isLoading(false)
                } else {
                    isLoading.postValue(false);
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                isLoading.postValue(false);
                facturas.postValue(facturasOriginales != null ? facturasOriginales : new ArrayList<>());
            }
        });
    }

    public void actualizarFiltros(InvoiceFilters filtros) {
        if (filtros.isValid()) {
            filtrosActuales.setValue(filtros);
            aplicarFiltros();
            errorValidacion.setValue(null);
        } else {
            errorValidacion.setValue("La fecha de inicio no puede ser posterior a la final.");
        }
    }

    private void aplicarFiltros() {
        InvoiceFilters filtros = filtrosActuales.getValue();
        if (filtros != null) {
            filtrarFacturas(
                    filtros.getEstadosSeleccionados(),
                    filtros.getFechaInicio(),
                    filtros.getFechaFin(),
                    filtros.getImporteMin(),
                    filtros.getImporteMax()
            );
        }
    }

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

    public void inicializarFiltros() {
        if (filtrosActuales.getValue() != null && !filtrosActuales.getValue().getEstadosSeleccionados().isEmpty()) {
            return;
        }
        InvoiceFilters filtros = new InvoiceFilters();
        LocalDate fechaAntigua = getOldestDate();
        if (fechaAntigua != null) {
            filtros.setFechaInicio(null);
            filtros.setFechaFin(null);
        }
        filtros.setImporteMin(0.0);
        filtros.setImporteMax((double) getMaxImporte());
        filtros.setEstadosSeleccionados(new ArrayList<>());
        filtrosActuales.postValue(filtros);
    }

    private void filtrarFacturas(List<String> estadosSeleccionados, LocalDate fechaInicio, LocalDate fechaFin, Double importeMin, Double importeMax) {
        // 1. Activar carga
        isLoading.setValue(true);

        new Thread(() -> {
            // Retardo para efecto visual suave
            //try { Thread.sleep(700); } catch (InterruptedException e) {}

            List<Invoice> listaBase = (facturasOriginales != null) ? facturasOriginales : new ArrayList<>();
            List<Invoice> facturasFiltradas = filterInvoicesUseCase.execute(listaBase, estadosSeleccionados, fechaInicio, fechaFin, importeMin, importeMax);

            facturas.postValue(facturasFiltradas);
            // 2. Desactivar carga
            isLoading.postValue(false);
        }).start();
    }

    // Métodos auxiliares
    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return 0f;
        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            if (factura.getImporteOrdenacion() > maxImporte) maxImporte = (float) factura.getImporteOrdenacion();
        }
        return maxImporte;
    }

    public LocalDate getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return null;
        LocalDate oldestDate = null;
        for (Invoice factura : facturasOriginales) {
            LocalDate currentDate = factura.getFecha();
            if (currentDate != null) {
                if (oldestDate == null || currentDate.isBefore(oldestDate)) oldestDate = currentDate;
            }
        }
        return oldestDate;
    }

    public LocalDate getNewestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return null;
        LocalDate newest = null;
        for (Invoice f : facturasOriginales) {
            if (f.getFecha() != null) {
                if (newest == null || f.getFecha().isAfter(newest)) newest = f.getFecha();
            }
        }
        return newest;
    }

    public boolean hayDatosCargados() {
        return facturasOriginales != null && !facturasOriginales.isEmpty();
    }

    public void actualizarEstadoFiltros(InvoiceFilters filtros) {
        this.filtrosActuales.setValue(filtros);
    }

    @VisibleForTesting
    public void setFacturasOriginalesTest(List<Invoice> facturas) {
        this.facturasOriginales = facturas;
        this.facturas.setValue(facturas);
    }
}
