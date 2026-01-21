package com.nexosolar.android.data.repository;

import android.util.Log;
import com.nexosolar.android.data.InvoiceMapper;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.source.InvoiceRemoteDataSource;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementación del repositorio de facturas con estrategia de caché local.
 *
 * Gestiona la lógica de obtención de facturas desde fuentes de datos remotas (API)
 * y locales (Room), implementando una estrategia de cache-first con fallback a red.
 *
 * Estrategias soportadas:
 * - Cache-first: Usa datos locales si existen, evitando llamadas innecesarias a la API
 * - Network-first: Fuerza recarga desde API (útil para desarrollo o pull-to-refresh)
 * - Fallback: Si la red falla, intenta recuperar datos de caché como respaldo
 *
 * Todas las operaciones de base de datos se ejecutan en un ExecutorService dedicado
 * para evitar bloquear el hilo principal.
 */
public class InvoiceRepositoryImpl implements InvoiceRepository {

    // ===== Constantes =====

    private static final String TAG = "FUENTE_DATOS";

    // ===== Variables de instancia =====

    private final InvoiceRemoteDataSource remoteDataSource;
    private final InvoiceDao localDataSource;
    private final InvoiceMapper mapper;
    private final ExecutorService executor;

    /**
     * Estrategia de caché: true = siempre ir a red (útil para mock/desarrollo)
     *                       false = usar caché local cuando esté disponible
     */
    private final boolean alwaysReload;

    // ===== Constructores =====

    public InvoiceRepositoryImpl(InvoiceRemoteDataSource remoteDataSource,
                                 InvoiceDao localDataSource,
                                 boolean alwaysReload) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
        this.mapper = new InvoiceMapper();
        this.executor = Executors.newSingleThreadExecutor();
        this.alwaysReload = alwaysReload;
    }

    // ===== Métodos públicos =====

    /**
     * Obtiene la lista de facturas aplicando la estrategia de caché configurada.
     *
     * Flujo de decisión:
     * 1. Si alwaysReload=true O no hay datos locales → ir a red
     * 2. Si hay datos locales → devolverlos inmediatamente (cache-first)
     * 3. Si falla la red → intentar recuperar de caché como fallback
     *
     * @param callback Callback para notificar el resultado de la operación
     */
    @Override
    public void getFacturas(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            List<InvoiceEntity> localData = localDataSource.getAllList();
            boolean hasData = localData != null && !localData.isEmpty();

            if (alwaysReload || !hasData) {
                Log.d(TAG, "🔄 Decisión: Ir a la RED (alwaysReload=" + alwaysReload + " o sin datos locales)");
                fetchFromNetwork(callback);
            } else {
                Log.d(TAG, "✅ DATOS RECUPERADOS DE ROOM (Caché rápida) - Total: " + localData.size());
                callback.onSuccess(mapper.toDomainList(localData));
            }
        });
    }

    /**
     * Fuerza una recarga de facturas desde la API, típicamente iniciada por pull-to-refresh.
     *
     * Siempre consulta la red independientemente de la estrategia de caché configurada.
     * Actualiza la base de datos local con los datos frescos recibidos.
     *
     * @param callback Callback opcional para notificar el resultado de la operación
     */
    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        Log.d(TAG, "🔄 Forzando recarga desde RED (Pull to Refresh)...");

        remoteDataSource.getFacturas(new RepositoryCallback<List<InvoiceEntity>>() {
            @Override
            public void onSuccess(List<InvoiceEntity> entities) {
                executor.execute(() -> {
                    Log.d(TAG, "✅ Recarga EXITOSA desde RED. Guardando " + entities.size() + " facturas.");
                    saveToDatabase(entities);
                    if (callback != null) callback.onSuccess(true);
                });
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "❌ Recarga FALLIDA: " + error.getMessage());
                if (callback != null) callback.onError(error);
            }
        });
    }

    // ===== Métodos privados =====

    /**
     * Obtiene facturas desde la API remota y gestiona el flujo de éxito/error.
     *
     * En caso de éxito: guarda en Room y devuelve los datos
     * En caso de error: intenta recuperar de caché local como fallback
     *
     * @param callback Callback para notificar el resultado
     */
    private void fetchFromNetwork(RepositoryCallback<List<Invoice>> callback) {
        remoteDataSource.getFacturas(new RepositoryCallback<List<InvoiceEntity>>() {
            @Override
            public void onSuccess(List<InvoiceEntity> entities) {
                executor.execute(() -> {
                    Log.d(TAG, "✅ DATOS RECIBIDOS - Total: " + entities.size());
                    saveToDatabase(entities);
                    Log.d(TAG, "💾 Datos guardados en ROOM");
                    callback.onSuccess(mapper.toDomainList(entities));
                });
            }

            @Override
            public void onError(Throwable error) {
                Log.e(TAG, "❌ FALLO RETROFIT: " + error.getMessage());
                Log.d(TAG, "🔄 Intentando recuperar de ROOM (Caché de emergencia)...");

                executor.execute(() -> {
                    List<InvoiceEntity> localData = localDataSource.getAllList();
                    if (localData != null && !localData.isEmpty()) {
                        Log.d(TAG, "✅ DATOS RECUPERADOS DE ROOM (Caché emergencia) - Total: " + localData.size());
                        callback.onSuccess(mapper.toDomainList(localData));
                    } else {
                        Log.e(TAG, "❌ ROOM ESTÁ VACÍO. No hay datos que mostrar.");
                        callback.onError(error);
                    }
                });
            }
        });
    }

    /**
     * Guarda facturas en la base de datos local reemplazando los datos existentes.
     *
     * Estrategia: deleteAll + insertAll para garantizar sincronización completa
     * sin datos obsoletos o duplicados.
     *
     * @param entities Lista de entidades a persistir
     */
    private void saveToDatabase(List<InvoiceEntity> entities) {
        localDataSource.deleteAll();
        localDataSource.insertAll(entities);
    }
}
