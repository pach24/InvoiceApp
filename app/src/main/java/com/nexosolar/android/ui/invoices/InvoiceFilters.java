package com.nexosolar.android.ui.invoices;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de datos que encapsula todos los criterios de filtrado de facturas.
 * Sigue el patrón MVVM manteniendo la lógica de validación separada de la UI.
 */
public class InvoiceFilters {
    private List<String> estadosSeleccionados;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double importeMin;
    private Double importeMax;

    public InvoiceFilters() {
        this.estadosSeleccionados = new ArrayList<>();
        this.fechaInicio = null;
        this.fechaFin = LocalDate.now();
        this.importeMin = 0.0;
        this.importeMax = 0.0;
    }

    // Getters
    public List<String> getEstadosSeleccionados() {
        return estadosSeleccionados;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Double getImporteMin() {
        return importeMin;
    }

    public Double getImporteMax() {
        return importeMax;
    }

    // Setters
    public void setEstadosSeleccionados(List<String> estados) {
        this.estadosSeleccionados = estados;
    }

    public void setFechaInicio(LocalDate fecha) {
        this.fechaInicio = fecha;
    }

    public void setFechaFin(LocalDate fecha) {
        this.fechaFin = fecha;
    }

    public void setImporteMin(Double importe) {
        this.importeMin = importe;
    }

    public void setImporteMax(Double importe) {
        this.importeMax = importe;
    }

    /**
     * Valida que los filtros sean lógicamente correctos.
     * @return true si los filtros son válidos, false en caso contrario.
     */
    public boolean isValid() {
        if (fechaInicio != null && fechaFin != null) {
            return !fechaInicio.isAfter(fechaFin);
        }
        return true;
    }
}
