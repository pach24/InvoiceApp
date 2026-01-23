package com.nexosolar.android.domain.usecase.invoice;

import com.nexosolar.android.domain.repository.RepositoryCallback;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.InvoiceRepository;

import java.util.List;

/**
 * Caso de uso encargado de obtener el listado de facturas.
 * Proporciona al ViewModel la lista de facturas
 */

public class GetInvoicesUseCase {
    private final InvoiceRepository repository;

    public GetInvoicesUseCase(InvoiceRepository repository) {
        this.repository = repository;
    }


    public void invoke(RepositoryCallback<List<Invoice>> callback) {
        repository.getInvoices(callback);
    }

    public void refresh(RepositoryCallback<Boolean> callback) {
        repository.refreshInvoices(callback);
    }
}
