package com.nexosolar.android.domain.repository;

import com.nexosolar.android.domain.models.Invoice;

import java.util.List;

public interface InvoiceRepository {
    void getFacturas(RepositoryCallback<List<Invoice>> callback);
    // Debe coincidir con la implementación:
    void refreshFacturas(RepositoryCallback<Boolean> callback);

}
