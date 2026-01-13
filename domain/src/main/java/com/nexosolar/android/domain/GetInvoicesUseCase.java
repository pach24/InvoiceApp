package com.nexosolar.android.domain;

import java.util.List;

public class GetInvoicesUseCase {
    private final InvoiceRepository repository;

    public GetInvoicesUseCase(InvoiceRepository repository) {
        this.repository = repository;
    }

    // El invoke ya no devuelve nada, recibe quien quiere escuchar
    public void invoke(RepositoryCallback<List<Invoice>> callback) {
        repository.getFacturas(callback);
    }

    // Cambiar el método refresh para aceptar el callback y pasarlo al repo
    public void refresh(RepositoryCallback<Boolean> callback) {
        repository.refreshFacturas(callback);
    }
}
