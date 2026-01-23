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

/**
 * Repositorio de facturas independiente de Android.
 */
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceRemoteDataSource remoteDataSource;
    private final InvoiceDao localDataSource;
    private final InvoiceMapper mapper;
    private final ExecutorService executor;
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
    public void getInvoices(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            List<InvoiceEntity> localData = localDataSource.getAllList();
            boolean hasData = localData != null && !localData.isEmpty();

            if (alwaysReload || !hasData) {
                fetchFromNetwork(callback, localData);
            } else {
                // Devuelve caché local
                callback.onSuccess(mapper.toDomainList(localData));
            }
        });
    }

    @Override
    public void refreshInvoices(RepositoryCallback<Boolean> callback) {
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

    private void fetchFromNetwork(RepositoryCallback<List<Invoice>> callback, List<InvoiceEntity> localCache) {
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
                executor.execute(() -> {
                    if (localCache != null && !localCache.isEmpty()) {
                        // Devuelve caché
                        callback.onSuccess(mapper.toDomainList(localCache));
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
