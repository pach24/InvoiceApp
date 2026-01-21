package com.nexosolar.android.domain.usecase.invoice;

import com.nexosolar.android.domain.repository.RepositoryCallback;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.InvoiceRepository;

import java.util.List;

/**
 * Caso de uso que aplica filtros múltiples al listado de facturas.
 * Implementa la lógica de negocio de filtrado por estado, fecha e importe de forma combinada.
 * Permite al ViewModel delegar la complejidad del filtrado y mantener la UI libre de lógica de negocio.
 */
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
