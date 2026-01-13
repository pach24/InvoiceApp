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

    // MutableLiveData simple para la lista que observa la UI
    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();

    // Lista auxiliar para no perder los datos originales al filtrar
    private List<Invoice> facturasOriginales = new ArrayList<>();

    private final GetInvoicesUseCase getInvoicesUseCase;

    public InvoiceViewModel(GetInvoicesUseCase useCase) {
        this.getInvoicesUseCase = useCase;
        // Cargar datos de la BD local al iniciar
        cargarFacturas();
    }

    /**
     * Pide al dominio la lista de facturas local (BD)
     */
    // 2. Asegúrate de que cargarFacturas llame a recarga solo si es necesario
    public void cargarFacturas() {
        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                if (result != null && !result.isEmpty()) {
                    facturasOriginales = result;
                    facturas.postValue(result);
                } else {
                    // Solo si la BD está vacía intentamos ir a internet
                    forzarRecarga();
                }
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
                facturas.postValue(new ArrayList<>());
            }
        });
    }

    public LiveData<List<Invoice>> getFacturas() {
        return facturas;
    }

    /**
     * Descarga datos de internet y actualiza la BD local
     */
    public void forzarRecarga() {
        getInvoicesUseCase.refresh(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                cargarFacturas(); // Si sale bien, recargamos la lista local
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                // Si falla la red, NO llamamos a cargarFacturas() otra vez para evitar bucles.
                // Simplemente paramos el loading avisando que no hay datos nuevos.
                facturas.postValue(facturasOriginales != null ? facturasOriginales : new ArrayList<>());
            }
        });
    }

    // --- LÓGICA DE FILTROS (Sin cambios) ---

    public void filtrarFacturas(List<String> estadosSeleccionados,
                                LocalDate fechaInicio,
                                LocalDate fechaFin,
                                Double importeMin,
                                Double importeMax) {

        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            facturas.setValue(new ArrayList<>());
            return;
        }

        List<Invoice> facturasFiltradas = new ArrayList<>();

        for (Invoice factura : facturasOriginales) {
            boolean cumpleEstado = (estadosSeleccionados == null ||
                    estadosSeleccionados.contains(factura.getDescEstado()));

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
                if (fechaInicio != null || fechaFin != null) {
                    cumpleFecha = false;
                }
            }

            boolean cumpleImporte = (importeMin == null || factura.getImporteOrdenacion() >= importeMin) &&
                    (importeMax == null || factura.getImporteOrdenacion() <= importeMax);

            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura);
            }
        }
        facturas.setValue(facturasFiltradas);
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
}
