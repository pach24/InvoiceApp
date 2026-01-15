package com.nexosolar.android.domain.usecase.invoice;

import com.nexosolar.android.domain.models.Invoice;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FilterInvoicesUseCase {

    public List<Invoice> execute(List<Invoice> facturasOriginales,
                                 List<String> estadosSeleccionados,
                                 LocalDate fechaInicio,
                                 LocalDate fechaFin,
                                 Double importeMin,
                                 Double importeMax) {

        // Si la lista original es nula o vacía, devolvemos lista vacía
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return new ArrayList<>();
        }

        List<Invoice> facturasFiltradas = new ArrayList<>();

        for (Invoice factura : facturasOriginales) {
            // 1. Filtro por Estado
            boolean cumpleEstado;
            if (estadosSeleccionados == null) {
                cumpleEstado = true;
            } else {
                // Si la lista de estados seleccionados contiene el estado de la factura
                cumpleEstado = estadosSeleccionados.contains(factura.getDescEstado());
            }

            // 2. Filtro por Fecha
            boolean cumpleFecha = true;
            LocalDate fechaFactura = factura.getFecha();

            if (fechaFactura != null) {
                if (fechaInicio != null) {
                    cumpleFecha = !fechaFactura.isBefore(fechaInicio);
                }
                if (cumpleFecha && fechaFin != null) {
                    cumpleFecha = !fechaFactura.isAfter(fechaFin);
                }
            } else {
                // Si hay rango pero la factura no tiene fecha, ¿se descarta?
                // Según tu lógica anterior:
                if (fechaInicio != null || fechaFin != null) {
                    cumpleFecha = false;
                }
            }

            // 3. Filtro por Importe
            double importe = factura.getImporteOrdenacion();
            boolean cumpleImporte = true;

            if (importeMin != null && importe < importeMin) {
                cumpleImporte = false;
            }
            if (cumpleImporte && importeMax != null && importe > importeMax) {
                cumpleImporte = false;
            }


            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura);
            }
        }

        return facturasFiltradas;
    }
}