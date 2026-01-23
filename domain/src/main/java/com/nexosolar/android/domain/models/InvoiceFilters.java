package com.nexosolar.android.domain.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de datos que encapsula todos los criterios de filtrado de facturas.
 * Sigue el patrón MVVM manteniendo la lógica de validación separada de la UI.
 * Centraliza el estado de los filtros para facilitar la comunicación entre ViewModel y UI
 */
public class InvoiceFilters {

    // ===== Variables de instancia =====
    private List<String> filteredStates;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double minAmount;
    private Double maxAmount;

    // ===== Constructores =====
    public InvoiceFilters() {
        this.filteredStates = new ArrayList<>();
        this.startDate = null;
        this.endDate = LocalDate.now();
        this.minAmount = 0.0;
        this.maxAmount = 0.0;
    }

    // Getters y Setters
    public List<String> getFilteredStates() {
        return filteredStates;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Double getMinAmount() {
        return minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    // Setters
    public void setFilteredStates(List<String> estados) {
        this.filteredStates = estados;
    }

    public void setStartDate(LocalDate fecha) {
        this.startDate = fecha;
    }

    public void setEndDate(LocalDate fecha) {
        this.endDate = fecha;
    }

    public void setMinAmount(Double importe) {
        this.minAmount = importe;
    }

    public void setMaxAmount(Double importe) {
        this.maxAmount = importe;
    }


    // ===== Métodos públicos =====
    /**
     * Valida que los filtros sean lógicamente correctos.
     * @return true si los filtros son válidos, false en caso contrario.
     */
    public boolean isValid() {
        if (startDate != null && endDate != null) {
            return !startDate.isAfter(endDate);
        }
        return true;
    }
}
