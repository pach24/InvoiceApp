package com.nexosolar.android.ui.invoices;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.nexosolar.android.data.InvoiceRepositoryImpl;
import com.nexosolar.android.domain.GetInvoicesUseCase;

public class InvoiceViewModelFactory implements ViewModelProvider.Factory {

    private final boolean useMock;
    private final Context context;

    public InvoiceViewModelFactory(boolean useMock, Context context) {
        this.useMock = useMock;
        this.context = context.getApplicationContext();
    } // <--- LLAVE DE CIERRE CORREGIDA

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InvoiceViewModel.class)) {
            // Crea el repositorio concreto
            InvoiceRepositoryImpl repository = new InvoiceRepositoryImpl(useMock, context);

            // Crea el caso de uso
            GetInvoicesUseCase useCase = new GetInvoicesUseCase(repository);

            // Crea el ViewModel
            return (T) new InvoiceViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
