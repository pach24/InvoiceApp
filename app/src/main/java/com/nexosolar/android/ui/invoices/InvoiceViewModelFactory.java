package com.nexosolar.android.ui.invoices;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

public class InvoiceViewModelFactory implements ViewModelProvider.Factory {

    private final InvoiceRepository repository;

    public InvoiceViewModelFactory(InvoiceRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InvoiceViewModel.class)) {
            return (T) new InvoiceViewModel(
                    new GetInvoicesUseCase(repository),
                    new FilterInvoicesUseCase()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
