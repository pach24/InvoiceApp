package com.nexosolar.android.domain;

import androidx.lifecycle.LiveData;
import com.nexosolar.android.data.InvoiceRepository;
import java.util.List;

public class GetInvoicesUseCase {

    private final InvoiceRepository repository;

    public GetInvoicesUseCase(InvoiceRepository repository) {
        this.repository = repository;
    }

    // 1. Obtener el flujo de datos (Lectura)
    public LiveData<List<Invoice>> invoke() {
        return repository.getFacturas();
    }

    // 2. Forzar actualización desde la nube (Escritura)
    public void refresh() {
        repository.refreshFacturas();
    }
}
