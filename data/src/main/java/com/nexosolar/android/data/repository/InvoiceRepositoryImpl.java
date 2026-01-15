package com.nexosolar.android.data.repository;

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

    // NUEVO: Bandera para decidir la estrategia de caché
    private final boolean alwaysReload;

    // Constructor actualizado
    public InvoiceRepositoryImpl(InvoiceRemoteDataSource remoteDataSource,
                                 InvoiceDao localDataSource,
                                 boolean alwaysReload) { // <--- Recibimos la preferencia
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

            // LÓGICA CORREGIDA:
            // Si nos piden recargar siempre (Mock) O si no tenemos datos locales... vamos a red.
            if (alwaysReload || !hasData) {
                fetchFromNetwork(callback);
            } else {
                // Si es modo real y ya tenemos datos, usamos la caché
                callback.onSuccess(mapper.toDomainList(localData));
            }
        });
    }

    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        remoteDataSource.getFacturas(new RepositoryCallback<List<InvoiceEntity>>() {
            @Override
            public void onSuccess(List<InvoiceEntity> entities) {
                executor.execute(() -> {
                    saveToDatabase(entities);
                    if (callback != null) callback.onSuccess(true);
                });
            }

            @Override
            public void onError(Throwable error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

    private void fetchFromNetwork(RepositoryCallback<List<Invoice>> callback) {
        remoteDataSource.getFacturas(new RepositoryCallback<List<InvoiceEntity>>() {
            @Override
            public void onSuccess(List<InvoiceEntity> entities) {
                executor.execute(() -> {
                    saveToDatabase(entities);
                    callback.onSuccess(mapper.toDomainList(entities));
                });
            }

            @Override
            public void onError(Throwable error) {
                // MEJORA OPCIONAL: Si falla la red (incluso en Mock), intentamos mostrar caché vieja
                // en lugar de error vacío.
                executor.execute(() -> {
                    List<InvoiceEntity> localData = localDataSource.getAllList();
                    if (localData != null && !localData.isEmpty()) {
                        callback.onSuccess(mapper.toDomainList(localData));
                    } else {
                        callback.onError(error);
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
