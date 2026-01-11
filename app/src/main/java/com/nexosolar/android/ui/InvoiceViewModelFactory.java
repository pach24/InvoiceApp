package com.nexosolar.android.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

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
            return (T) new InvoiceViewModel(useMock, context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
