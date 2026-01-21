package com.nexosolar.android.data.source;

import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import java.util.List;

/**
 * Interfaz para el origen de datos remoto de facturas.
 *
 * Abstrae la capa de red para facilitar testing y permitir implementaciones
 * alternativas (API real, mock local, etc.) sin afectar al repositorio.
 *
 * Devuelve InvoiceEntity en lugar de modelos de dominio para mantener
 * separación de responsabilidades: el DataSource solo obtiene datos,
 * el Repository se encarga del mapeo a dominio.
 */
public interface InvoiceRemoteDataSource {

    /**
     * Obtiene la lista de facturas desde la fuente remota.
     *
     * @param callback Callback para notificar el resultado (lista de entidades o error)
     */
    void getFacturas(RepositoryCallback<List<InvoiceEntity>> callback);
}
