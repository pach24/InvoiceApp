package com.nexosolar.android.ui.invoices;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.data.DataModule; // Solo esto
import com.nexosolar.android.domain.repository.InvoiceRepository; // Solo interfaz
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

public class InvoiceViewModelFactory implements ViewModelProvider.Factory {

    private final boolean useMock;
    private final Context context;

    public InvoiceViewModelFactory(boolean useMock, Context context) {
        this.useMock = useMock;
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InvoiceViewModel.class)) {

            // 1. Usar el DataModule para obtener el repositorio ya armado
            DataModule dataModule = new DataModule(context, useMock);
            InvoiceRepository repository = dataModule.provideInvoiceRepository();

            // 2. Crear Use Cases
            GetInvoicesUseCase getInvoicesUseCase = new GetInvoicesUseCase(repository);
            FilterInvoicesUseCase filterInvoicesUseCase = new FilterInvoicesUseCase();

            return (T) new InvoiceViewModel(getInvoicesUseCase, filterInvoicesUseCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
