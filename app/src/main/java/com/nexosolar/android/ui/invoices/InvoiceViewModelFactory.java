package com.nexosolar.android.ui.invoices;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.data.DataModule;
import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

/**
 * InvoiceViewModelFactory
 *
 * Factory para la creación de InvoiceViewModel con inyección de dependencias manual.
 * Gestiona la configuración del repositorio (Mock o Real) y la construcción de Use Cases.
 */
public class InvoiceViewModelFactory implements ViewModelProvider.Factory {

    // ===== Variables de instancia =====

    private final boolean useMock;
    private final Context context;

    // ===== Constructor =====

    public InvoiceViewModelFactory(boolean useMock, Context context) {
        this.useMock = useMock;
        this.context = context.getApplicationContext();
    }

    // ===== Método de creación =====

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InvoiceViewModel.class)) {
            DataModule dataModule = new DataModule(context, useMock);
            InvoiceRepository repository = dataModule.provideInvoiceRepository();

            GetInvoicesUseCase getInvoicesUseCase = new GetInvoicesUseCase(repository);
            FilterInvoicesUseCase filterInvoicesUseCase = new FilterInvoicesUseCase();

            return (T) new InvoiceViewModel(getInvoicesUseCase, filterInvoicesUseCase);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
