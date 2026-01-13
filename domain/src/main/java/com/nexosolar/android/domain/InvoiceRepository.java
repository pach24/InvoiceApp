package com.nexosolar.android.domain;

import java.util.List;

public interface InvoiceRepository {
    void getFacturas(RepositoryCallback<List<Invoice>> callback);
    // Debe coincidir con la implementación:
    void refreshFacturas(RepositoryCallback<Boolean> callback);

}
