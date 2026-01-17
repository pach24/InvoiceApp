package com.nexosolar.android.data.repository;

import android.util.Log; // Importante

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

public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceRemoteDataSource remoteDataSource;
    private final InvoiceDao localDataSource;
    private final InvoiceMapper mapper;
    private final ExecutorService executor;

    // Bandera para decidir la estrategia de caché
    private final boolean alwaysReload;

    public InvoiceRepositoryImpl(InvoiceRemoteDataSource remoteDataSource,
                                 InvoiceDao localDataSource,
                                 boolean alwaysReload) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
        this.mapper = new InvoiceMapper();
        this.executor = Executors.newSingleThreadExecutor();
        this.alwaysReload = alwaysReload;
    }

    @Override
    public void getFacturas(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            List<InvoiceEntity> localData = localDataSource.getAllList();
            boolean hasData = localData != null && !localData.isEmpty();

            // Si nos piden recargar siempre (Mock) O si no tenemos datos locales... vamos a red.
            if (alwaysReload || !hasData) {
                Log.d("FUENTE_DATOS", "🔄 Decisión: Ir a la RED (alwaysReload=" + alwaysReload + " o sin datos locales)");
                fetchFromNetwork(callback);
            } else {
                // Si es modo real y ya tenemos datos, usamos la caché directamente
                Log.d("FUENTE_DATOS", "✅ DATOS RECUPERADOS DE ROOM (Caché rápida) - Total: " + localData.size());
                callback.onSuccess(mapper.toDomainList(localData));
            }
        });
    }

    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        Log.d("FUENTE_DATOS", "🔄 Forzando recarga desde RED (Pull to Refresh)...");
        remoteDataSource.getFacturas(new RepositoryCallback<List<InvoiceEntity>>() {
            @Override
            public void onSuccess(List<InvoiceEntity> entities) {
                executor.execute(() -> {
                    Log.d("FUENTE_DATOS", "✅ Recarga EXITOSA desde RED. Guardando " + entities.size() + " facturas.");
                    saveToDatabase(entities);
                    if (callback != null) callback.onSuccess(true);
                });
            }

            @Override
            public void onError(Throwable error) {
                Log.e("FUENTE_DATOS", "❌ Recarga FALLIDA: " + error.getMessage());
                if (callback != null) callback.onError(error);
            }
        });
    }


    private void fetchFromNetwork(RepositoryCallback<List<Invoice>> callback) {
        remoteDataSource.getFacturas(new RepositoryCallback<List<InvoiceEntity>>() {
            @Override
            public void onSuccess(List<InvoiceEntity> entities) {
                executor.execute(() -> {
                    // 1. ÉXITO DE RED
                    Log.d("FUENTE_DATOS", "✅ DATOS RECIBIDOS - Total: " + entities.size());

                    saveToDatabase(entities);
                    Log.d("FUENTE_DATOS", "💾 Datos guardados en ROOM");

                    callback.onSuccess(mapper.toDomainList(entities));
                });
            }

            @Override
            public void onError(Throwable error) {
                // 2. FALLO DE RED -> INTENTAR CACHÉ (Fallback)
                Log.e("FUENTE_DATOS", "❌ FALLO RETROFIT: " + error.getMessage());
                Log.d("FUENTE_DATOS", "🔄 Intentando recuperar de ROOM (Caché de emergencia)...");

                executor.execute(() -> {
                    List<InvoiceEntity> localData = localDataSource.getAllList();
                    if (localData != null && !localData.isEmpty()) {

                        Log.d("FUENTE_DATOS", "✅ DATOS RECUPERADOS DE ROOM (Caché emergencia) - Total: " + localData.size());
                        callback.onSuccess(mapper.toDomainList(localData));

                    } else {
                        Log.e("FUENTE_DATOS", "❌ ROOM ESTÁ VACÍO. No hay datos que mostrar.");
                        callback.onError(error); // Ahora sí devolvemos el error porque no tenemos nada
                    }
                });
            }
        });
    }

    private void saveToDatabase(List<InvoiceEntity> entities) {
        localDataSource.deleteAll();
        localDataSource.insertAll(entities);
    }
}
