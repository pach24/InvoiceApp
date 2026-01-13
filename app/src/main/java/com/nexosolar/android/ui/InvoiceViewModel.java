package com.nexosolar.android.ui;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.nexosolar.android.data.InvoiceRepository;
import com.nexosolar.android.domain.GetInvoicesUseCase; // IMPORTANTE
import com.nexosolar.android.domain.Invoice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.VisibleForTesting;

public class InvoiceViewModel extends ViewModel {

    // LiveData principal
    private final MediatorLiveData<List<Invoice>> facturas = new MediatorLiveData<>();

    // Copia local para filtros
    private List<Invoice> facturasOriginales = new ArrayList<>();

    // CAMBIO: Declaramos el Caso de Uso (NO el repositorio)
    private final GetInvoicesUseCase getInvoicesUseCase;

    public InvoiceViewModel(GetInvoicesUseCase useCase) {
        this.getInvoicesUseCase = useCase;

        // Lógica de conexión (Igual que antes)
        facturas.addSource(getInvoicesUseCase.invoke(), listaDeRoom -> {
            if (listaDeRoom != null) {
                this.facturasOriginales = listaDeRoom;
                this.facturas.setValue(listaDeRoom);
            }
        });

        getInvoicesUseCase.refresh();
    }

    public LiveData<List<Invoice>> getFacturas() {
        return facturas;
    }

    public void forzarRecarga() {
        // CAMBIO: Ahora usamos el UseCase, no el repositorio directo
        getInvoicesUseCase.refresh();
    }

    // --- LÓGICA DE FILTROS (Sin cambios) ---

    public List<Invoice> filtrarFacturas(List<String> estadosSeleccionados,
                                         LocalDate fechaInicio,
                                         LocalDate fechaFin,
                                         Double importeMin,
                                         Double importeMax) {

        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            facturas.setValue(new ArrayList<>());
            return new ArrayList<>();
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
        return facturasFiltradas;
    }

    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return 0f;
        }
        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            if (factura.getImporteOrdenacion() > maxImporte) {
                maxImporte = factura.getImporteOrdenacion();
            }
        }
        return maxImporte;
    }

    public LocalDate getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return null;
        }
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


