package com.nexosolar.android.domain.usecase.invoice;

import com.nexosolar.android.domain.models.Invoice;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


/**
 * Caso de uso que aplica filtros múltiples al listado de facturas.
 * Implementa la lógica de negocio de filtrado por estado, fecha e importe de forma combinada.
 * Permite al ViewModel delegar la complejidad del filtrado y mantener la UI libre de lógica de negocio.
 */
public class FilterInvoicesUseCase {


    // ===== Métodos públicos =====

    /**
     * Filtra el listado de facturas según los criterios proporcionados.
     * Los filtros son acumulativos (AND lógico): una factura debe cumplir todos los criterios activos.
     *
     * @param facturasOriginales Lista completa de facturas sin filtrar
     * @param estadosSeleccionados Estados permitidos (null = sin filtro de estado)
     * @param fechaInicio Fecha mínima (inclusive)
     * @param fechaFin Fecha máxima (inclusive)
     * @param importeMin Importe mínimo (inclusive)
     * @param importeMax Importe máximo (inclusive)
     * @return Lista filtrada según los criterios aplicados
     */
    public List<Invoice> execute(List<Invoice> facturasOriginales,
                                 List<String> estadosSeleccionados,
                                 LocalDate fechaInicio,
                                 LocalDate fechaFin,
                                 Double importeMin,
                                 Double importeMax) {


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
                cumpleEstado = estadosSeleccionados.contains(factura.getInvoiceStatus());
            }

            // 2. Filtro por Fecha
            boolean cumpleFecha = true;
            LocalDate fechaFactura = factura.getInvoiceDate();

            if (fechaFactura != null) {
                if (fechaInicio != null) {
                    cumpleFecha = !fechaFactura.isBefore(fechaInicio);
                }
                if (cumpleFecha && fechaFin != null) {
                    cumpleFecha = !fechaFactura.isAfter(fechaFin);
                }
            } else {
                // Si hay rango pero la factura no tiene fecha, ¿se descarta?
                if (fechaInicio != null || fechaFin != null) {
                    cumpleFecha = false;
                }
            }

            // 3. Filtro por Importe
            double importe = factura.getInvoiceAmount();
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