package com.nexosolar.android.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.nexosolar.android.data.InvoiceRepository;
import com.nexosolar.android.domain.GetInvoicesUseCase;

    /*
    Para poder tener viewmodels con constructores personalizados (con boolean y context),
    creamos esta clase, que funciona para crear instancias del InvoiceViewModel
     */

public class InvoiceViewModelFactory implements ViewModelProvider.Factory {
    private final boolean useMock;
    private final Context context;
    public InvoiceViewModelFactory(boolean useMock, Context context) {
        this.useMock = useMock;
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InvoiceViewModel.class)) {
            // 1. Aquí creamos las dependencias reales (Base de datos, Repo...)
            InvoiceRepository repository = new InvoiceRepository(useMock, context);
            GetInvoicesUseCase useCase = new GetInvoicesUseCase(repository);

            // 2. Se las pasamos limpias al ViewModel
            return (T) new InvoiceViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
