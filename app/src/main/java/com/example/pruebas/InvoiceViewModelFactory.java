package com.example.pruebas;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class InvoiceViewModelFactory implements ViewModelProvider.Factory {
    private final boolean useMock;
    private final Context context;

    public InvoiceViewModelFactory(boolean useMock, Context context) {
        this.useMock = useMock;
        this.context = context.getApplicationContext(); // Evitar fugas de memoria
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
