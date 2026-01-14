package com.nexosolar.android.ui.invoices;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexosolar.android.domain.GetInvoicesUseCase;
import com.nexosolar.android.domain.Invoice;
import com.nexosolar.android.domain.RepositoryCallback;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceViewModel extends ViewModel {

    // LiveData para la lista de facturas que observa la UI
    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();

    // Lista auxiliar para no perder los datos originales al filtrar
    private List<Invoice> facturasOriginales = new ArrayList<>();

    private final GetInvoicesUseCase getInvoicesUseCase;

    // BANDERA: Controla que la recarga automática solo ocurra una vez al inicio
    private boolean isFirstLoad = true;

    // --- NUEVOS LiveData para MVVM ---
    private final MutableLiveData<InvoiceFilters> filtrosActuales = new MutableLiveData<>(new InvoiceFilters());
    private final MutableLiveData<String> errorValidacion = new MutableLiveData<>();

    public InvoiceViewModel(GetInvoicesUseCase useCase) {
        this.getInvoicesUseCase = useCase;
        cargarFacturas();
    }

    /**
     * Pide al dominio la lista de facturas local (BD)
     */
    public void cargarFacturas() {
        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                if (result != null) {
                    facturasOriginales = result;
                    facturas.postValue(result);
                }

                if (isFirstLoad) {
                    isFirstLoad = false;
                    if (result == null || result.isEmpty()) {
                        forzarRecarga();
                    }
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

    /**
     * Descarga datos de internet (o Mock) y actualiza la BD local
     */
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
                facturas.postValue(facturasOriginales != null ? facturasOriginales : new ArrayList<>());
            }
        });
    }

    // --- NUEVOS MÉTODOS PARA MVVM ---

    /**
     * Expone los filtros actuales a la UI
     */
    public LiveData<InvoiceFilters> getFiltrosActuales() {
        return filtrosActuales;
    }

    /**
     * Expone errores de validación a la UI
     */
    public LiveData<String> getErrorValidacion() {
        return errorValidacion;
    }

    /**
     * Actualiza los filtros y aplica el filtrado si son válidos
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
     * Aplica los filtros actuales a la lista de facturas
     */
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

    /**
     * Acción del botón "Borrar": Limpia filtros visuales y muestra todo.
     */
    public void resetearFiltros() {
        InvoiceFilters nuevosFiltros = new InvoiceFilters();

        // Resetear Fechas
        LocalDate fechaAntigua = getOldestDate();
        if (fechaAntigua != null) {
            nuevosFiltros.setFechaInicio(fechaAntigua);
        }
        nuevosFiltros.setFechaFin(LocalDate.now());

        // Resetear Importes
        nuevosFiltros.setImporteMin(0.0);
        nuevosFiltros.setImporteMax((double) getMaxImporte());

        // Resetear Estados: LISTA VACÍA (Checkboxes OFF)
        nuevosFiltros.setEstadosSeleccionados(new ArrayList<>());

        // Actualizar UI (FilterFragment observará esto y desmarcará los checkbox)
        filtrosActuales.setValue(nuevosFiltros);

        // Restaurar lista completa de facturas en la Activity
        if (facturasOriginales != null) {
            facturas.setValue(new ArrayList<>(facturasOriginales));
        }
    }

    /**
     * Inicializa los filtros con valores por defecto al abrir el fragmento
     */
    public void inicializarFiltros() {
        // Solo sobrescribir si no hay filtros previos configurados (opcional, según tu flujo)
        if (filtrosActuales.getValue() != null && !filtrosActuales.getValue().getEstadosSeleccionados().isEmpty()) {
            return; // Ya hay filtros configurados, no tocamos nada.
        }

        InvoiceFilters filtros = new InvoiceFilters();

        // Fechas
        LocalDate fechaAntigua = getOldestDate();
        if (fechaAntigua != null) {
            filtros.setFechaInicio(fechaAntigua);
        }
        filtros.setFechaFin(LocalDate.now());

        // Importes
        filtros.setImporteMin(0.0);
        filtros.setImporteMax((double) getMaxImporte());

        // Estados: TODOS marcados por defecto
        List<String> todosEstados = new ArrayList<>();
        todosEstados.add("Pagada");
        todosEstados.add("Pendiente de pago");
        todosEstados.add("Anulada");
        todosEstados.add("Cuota fija");
        todosEstados.add("Plan de pago");
        filtros.setEstadosSeleccionados(todosEstados);

        filtrosActuales.setValue(filtros);
    }

    // --- LÓGICA DE FILTROS ---

    private void filtrarFacturas(List<String> estadosSeleccionados,
                                 LocalDate fechaInicio,
                                 LocalDate fechaFin,
                                 Double importeMin,
                                 Double importeMax) {

        // Ejecutar en hilo secundario para no bloquear la UI
        new Thread(() -> {
            // Copia de seguridad de la lista original
            List<Invoice> listaBase = new ArrayList<>(facturasOriginales != null ? facturasOriginales : new ArrayList<>());

            if (listaBase.isEmpty()) {
                facturas.postValue(new ArrayList<>());
                return;
            }

            List<Invoice> facturasFiltradas = new ArrayList<>();

            for (Invoice factura : listaBase) {
                // 1. Filtro por Estado (Lógica Estricta)
                boolean cumpleEstado;
                if (estadosSeleccionados == null) {
                    // Si es null, asumimos que no se ha aplicado filtro aún -> pasa todo
                    cumpleEstado = true;
                } else {
                    // Si la lista existe (aunque esté vacía), el estado DEBE estar en ella.
                    // Si está vacía, contains retorna false -> no muestra nada.
                    cumpleEstado = estadosSeleccionados.contains(factura.getDescEstado());
                }

                // 2. Filtro por Fecha
                boolean cumpleFecha = true;
                LocalDate fechaFactura = factura.getFecha();

                if (fechaFactura != null) {
                    if (fechaInicio != null) {
                        cumpleFecha &= !fechaFactura.isBefore(fechaInicio);
                    }
                    if (fechaFin != null) {
                        cumpleFecha &= !fechaFactura.isAfter(fechaFin);
                    }
                } else {
                    // Si estamos filtrando por rango y la factura no tiene fecha, la descartamos
                    if (fechaInicio != null || fechaFin != null) {
                        cumpleFecha = false;
                    }
                }

                // 3. Filtro por Importe
                double importe = factura.getImporteOrdenacion();
                boolean cumpleImporte = (importeMin == null || importe >= importeMin) &&
                        (importeMax == null || importe <= importeMax);

                // Solo añadir si cumple TODAS las condiciones
                if (cumpleEstado && cumpleFecha && cumpleImporte) {
                    facturasFiltradas.add(factura);
                }
            }

            // Publicar resultado en el hilo principal
            facturas.postValue(facturasFiltradas);

        }).start();
    }


    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) return 0f;
        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            if (factura.getImporteOrdenacion() > maxImporte) {
                maxImporte = factura.getImporteOrdenacion();
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

    public boolean hayDatosCargados() {
        return facturasOriginales != null && !facturasOriginales.isEmpty();
    }

    @VisibleForTesting
    public void setFacturasOriginalesTest(List<Invoice> facturas) {
        this.facturasOriginales = facturas;
        this.facturas.setValue(facturas);
    }

    /**
     * Métod0 auxiliar para actualizar el estado visual de los filtros
     * (por ejemplo, al cambiar una fecha) sin aplicar todavía el filtrado a la lista.
     */
    public void actualizarEstadoFiltros(InvoiceFilters filtros) {
        this.filtrosActuales.setValue(filtros);
    }
}
