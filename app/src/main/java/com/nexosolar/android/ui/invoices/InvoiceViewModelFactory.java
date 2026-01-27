package com.nexosolar.android.ui.invoices;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

// Estructura correcta de tu Factory (file:24)
public class InvoiceViewModelFactory implements ViewModelProvider.Factory {
    private final GetInvoicesUseCase getInvoicesUseCase;
    private final FilterInvoicesUseCase filterInvoicesUseCase;

    public InvoiceViewModelFactory(GetInvoicesUseCase getInvoices, FilterInvoicesUseCase filterInvoices) {
        this.getInvoicesUseCase = getInvoices;
        this.filterInvoicesUseCase = filterInvoices;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new InvoiceViewModel(getInvoicesUseCase, filterInvoicesUseCase);
    }
}

