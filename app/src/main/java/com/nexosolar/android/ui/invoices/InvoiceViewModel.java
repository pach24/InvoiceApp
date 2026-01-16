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
    // Lista auxiliar para no perder los datos originales al filtrar
    private List<Invoice> facturasOriginales = new ArrayList<>();

    // Casos de uso
    private final GetInvoicesUseCase getInvoicesUseCase;
    private final FilterInvoicesUseCase filterInvoicesUseCase; // <--- NUEVO

    // BANDERA: Controla que la recarga automática solo ocurra una vez al inicio
    private boolean isFirstLoad = true;

    // --- LIVE DATA PARA ESTADO DE FILTROS ---
    private final MutableLiveData<InvoiceFilters> filtrosActuales = new MutableLiveData<>(new InvoiceFilters());
    private final MutableLiveData<String> errorValidacion = new MutableLiveData<>();


    public InvoiceViewModel(GetInvoicesUseCase getInvoicesUseCase, FilterInvoicesUseCase filterInvoicesUseCase) {
        this.getInvoicesUseCase = getInvoicesUseCase;
        this.filterInvoicesUseCase = filterInvoicesUseCase;
        cargarFacturas();
    }

    // Pide al dominio la lista de facturas
    public void cargarFacturas() {
        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                if (result != null) {
                    facturasOriginales = result;
                    facturas.postValue(result);

                    // Inicializamos filtros si es la primera carga y hay datos
                    if (isFirstLoad) {
                        isFirstLoad = false;
                        inicializarFiltros();
                    }
                } else if (isFirstLoad) {
                    isFirstLoad = false;
                    forzarRecarga();
                }
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                if (isFirstLoad) {
                    isFirstLoad = false;
                    forzarRecarga();
                } else {
                    facturas.postValue(new ArrayList<>());
                }
            }
        });
    }

    public LiveData<List<Invoice>> getFacturas() {
        return facturas;
    }

    public void forzarRecarga() {
        getInvoicesUseCase.refresh(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                if (Boolean.TRUE.equals(success)) {
                    cargarFacturas();
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                // Si falla la red, mostramos lo que tengamos en memoria/cache
                facturas.postValue(facturasOriginales != null ? facturasOriginales : new ArrayList<>());
            }
        });
    }

    // --- MÉTODOS PARA GESTIÓN DE FILTROS ---

    public LiveData<InvoiceFilters> getFiltrosActuales() {
        return filtrosActuales;
    }

    public LiveData<String> getErrorValidacion() {
        return errorValidacion;
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

        // Resetear Fechas
        // 1. FECHAS A NULL (Crucial para que salga día/mes/año)
        nuevosFiltros.setFechaInicio(null);
        nuevosFiltros.setFechaFin(null);



        // Resetear Importes
        nuevosFiltros.setImporteMin(0.0);
        nuevosFiltros.setImporteMax((double) getMaxImporte());

        // Resetear Estados (Checkboxes OFF)
        nuevosFiltros.setEstadosSeleccionados(new ArrayList<>());

        filtrosActuales.setValue(nuevosFiltros);

        // Restaurar lista completa
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
            filtros.setFechaInicio(fechaAntigua);
            filtros.setFechaFin(LocalDate.now());
        }

        filtros.setFechaInicio(null);
        filtros.setFechaFin(null);


        filtros.setImporteMin(0.0);
        filtros.setImporteMax((double) getMaxImporte());


        filtros.setEstadosSeleccionados(new ArrayList<>());


        filtrosActuales.postValue(filtros);
    }


    // --- LÓGICA DE FILTRADO DELEGADA AL USECASE ---

    private void filtrarFacturas(List<String> estadosSeleccionados, LocalDate fechaInicio, LocalDate fechaFin, Double importeMin, Double importeMax) {
        // Ejecutar en hilo secundario para no bloquear la UI
        new Thread(() -> {
            // Copia de seguridad de la lista original
            List<Invoice> listaBase = (facturasOriginales != null) ? facturasOriginales : new ArrayList<>();

            // LLAMADA AL CASO DE USO
            List<Invoice> facturasFiltradas = filterInvoicesUseCase.execute(
                    listaBase,
                    estadosSeleccionados,
                    fechaInicio,
                    fechaFin,
                    importeMin,
                    importeMax
            );

            // Publicar resultado en el hilo principal
            facturas.postValue(facturasFiltradas);
        }).start();
    }

    // Métodos auxiliares de UI (máximos y mínimos)
    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return 0f;
        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            if (factura.getImporteOrdenacion() > maxImporte) {
                maxImporte = (float) factura.getImporteOrdenacion();
            }
        }
        return maxImporte;
    }

    public LocalDate getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return null;
        LocalDate oldestDate = null;
        for (Invoice factura : facturasOriginales) {
            LocalDate currentDate = factura.getFecha();
            if (currentDate != null) {
                if (oldestDate == null || currentDate.isBefore(oldestDate)) {
                    oldestDate = currentDate;
                }
            }
        }
        return oldestDate;
    }

    public LocalDate getNewestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return null;
        LocalDate newest = null;
        for (Invoice f : facturasOriginales) {
            if (f.getFecha() != null) {
                if (newest == null || f.getFecha().isAfter(newest)) {
                    newest = f.getFecha();
                }
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
