package com.nexosolar.android.ui.invoices.managers;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.nexosolar.android.core.DateValidator;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor especializado en el ESTADO de los filtros.
 *
 * - SRP: Solo gestiona el estado y orquesta el filtrado.
 * - Delegación: Usa DateValidator para validación y InvoiceStatisticsCalculator para valores por defecto.
 */
public class InvoiceFilterManager {

    private final FilterInvoicesUseCase filterUseCase;
    private final InvoiceStatisticsCalculator calculator;

    private final MutableLiveData<InvoiceFilters> _currentFilters = new MutableLiveData<>();
    private final MutableLiveData<String> _validationError = new MutableLiveData<>();

    public InvoiceFilterManager(FilterInvoicesUseCase filterUseCase) {
        this.filterUseCase = filterUseCase;
        this.calculator = new InvoiceStatisticsCalculator(); // Podría inyectarse también
        initializeDefaultFilters();
    }

    // ===== Getters =====

    public LiveData<InvoiceFilters> getCurrentFilters() {
        return _currentFilters;
    }

    public LiveData<String> getValidationError() {
        return _validationError;
    }

    // ===== Gestión de Estado =====

    public void updateFilters(InvoiceFilters filters) {
        if (filters == null) {
            _validationError.setValue("Los filtros no pueden ser nulos");
            return;
        }

        // USO DE DATE VALIDATOR: Validación delegada
        if (DateValidator.isValidRange(filters.getStartDate(), filters.getEndDate())) {
            _currentFilters.setValue(filters);
            _validationError.setValue(null);
        } else {
            _validationError.setValue("La fecha de inicio no puede ser posterior a la fecha final");
        }
    }

    public void resetFilters(List<Invoice> invoices) {
        InvoiceFilters defaultFilters = new InvoiceFilters();

        defaultFilters.setStartDate(null);
        defaultFilters.setEndDate(null);
        defaultFilters.setMinAmount(0.0);

        // USO DE CALCULATOR: Delegamos el cálculo del máximo
        float maxAmount = calculator.calculateMaxAmount(invoices);
        defaultFilters.setMaxAmount((double) maxAmount);

        defaultFilters.setFilteredStates(new ArrayList<>());

        _currentFilters.setValue(defaultFilters);
        _validationError.setValue(null);
    }

    // ===== Ejecución de Filtros =====

    public List<Invoice> applyCurrentFilters(List<Invoice> invoices) {
        InvoiceFilters filters = _currentFilters.getValue();
        if (filters == null || invoices == null || invoices.isEmpty()) {
            return new ArrayList<>();
        }

        return filterUseCase.execute(
                invoices,
                filters.getFilteredStates(),
                filters.getStartDate(),
                filters.getEndDate(),
                filters.getMinAmount(),
                filters.getMaxAmount()
        );
    }

    // ===== Métodos de Consulta de Estado =====

    public boolean hasActiveFilters() {
        InvoiceFilters filters = _currentFilters.getValue();
        if (filters == null) return false;

        if (filters.getFilteredStates() != null && !filters.getFilteredStates().isEmpty()) return true;
        if (filters.getStartDate() != null || filters.getEndDate() != null) return true;

        // Comprobamos si el rango es distinto al por defecto (0 - MAX)
        return filters.getMinAmount() > 0 ||
                (filters.getMaxAmount() != null && filters.getMaxAmount() < Double.MAX_VALUE);
    }

    private void initializeDefaultFilters() {
        InvoiceFilters defaultFilters = new InvoiceFilters();
        defaultFilters.setMinAmount(0.0);
        defaultFilters.setMaxAmount(Double.MAX_VALUE);
        defaultFilters.setFilteredStates(new ArrayList<>());
        _currentFilters.setValue(defaultFilters);
    }
}