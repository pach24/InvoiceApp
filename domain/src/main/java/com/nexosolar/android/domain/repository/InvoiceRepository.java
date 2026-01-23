package com.nexosolar.android.domain.repository;

import com.nexosolar.android.domain.models.Invoice;

import java.util.List;

/**
 * Contrato de repositorio para la gestión de facturas.
 * Define las operaciones de lectura y sincronización disponibles para los casos de uso.
 */
public interface InvoiceRepository {

    // ===== Métodos públicos =====

    /**
     * Obtiene el listado de facturas.
     * La implementación decidirá la estrategia de cache (Single Source of Truth).
     */
    void getInvoices(RepositoryCallback<List<Invoice>> callback);


    /**
     * Fuerza una actualización de datos desde la fuente remota.
     */

    void refreshInvoices(RepositoryCallback<Boolean> callback);

}
