package com.nexosolar.android.data.repository;

import android.content.SharedPreferences;
import android.util.Log;

import com.nexosolar.android.data.InvoiceMapper;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.remote.InvoiceResponse;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.repository.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRepositoryImpl implements InvoiceRepository {

    private static final String TAG = "DEBUG_REPO";
    private static final String KEY_LAST_MODE_WAS_MOCK = "last_mode_was_mock";

    private final ApiService apiService;
    private final InvoiceDao invoiceDao;
    private final SharedPreferences prefs;
    private final boolean isMockMode;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final InvoiceMapper invoiceMapper; // <--- NUEVO CAMPO

    public InvoiceRepositoryImpl(ApiService apiService,
                                 InvoiceDao invoiceDao,
                                 SharedPreferences prefs,
                                 boolean isMockMode) {
        this.apiService = apiService;
        this.invoiceDao = invoiceDao;
        this.prefs = prefs;
        this.isMockMode = isMockMode;
        this.invoiceMapper = new InvoiceMapper(); // <--- INICIALIZACIÓN (o inyectar)

        checkAndClearIfModeChangedSync();
    }

    private void checkAndClearIfModeChangedSync() {
        try {
            executor.submit(() -> {
                boolean lastModeWasMock = prefs.getBoolean(KEY_LAST_MODE_WAS_MOCK, false);
                if (isMockMode != lastModeWasMock) {
                    Log.w(TAG, "*** CAMBIO DE MODO DETECTADO *** Limpiando BD...");
                    invoiceDao.deleteAll();
                    prefs.edit().putBoolean(KEY_LAST_MODE_WAS_MOCK, isMockMode).apply();
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getFacturas(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            if (isMockMode) {
                // MODO MOCK: Forzar refresh
                refreshFacturas(new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) { readFromDatabase(callback); }
                    @Override
                    public void onError(Throwable t) { callback.onError(t); }
                });
            } else {
                // MODO REAL: Cache First
                List<InvoiceEntity> entities = invoiceDao.getAllList();
                if (entities != null && !entities.isEmpty()) {
                    List<Invoice> domainList = invoiceMapper.toDomainList(entities);
                    callback.onSuccess(domainList);
                } else {
                    refreshFacturas(new RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) { readFromDatabase(callback); }
                        @Override
                        public void onError(Throwable t) { callback.onError(t); }
                    });
                }
            }
        });
    }

    private void readFromDatabase(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            List<InvoiceEntity> entities = invoiceDao.getAllList();
            List<Invoice> domainList = new ArrayList<>();
            if (entities != null) {
                domainList = invoiceMapper.toDomainList(entities);
                callback.onSuccess(domainList);
            }
            callback.onSuccess(domainList);
        });
    }

    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executor.execute(() -> {
                        try {
                            if (isMockMode) invoiceDao.deleteAll(); // Solo borrar todo en mock, o según lógica deseada
                            // Nota: En real quizás quieras un upsert, pero deleteAll es seguro para evitar duplicados simples
                            if (!isMockMode) invoiceDao.deleteAll();


                            List<InvoiceEntity> entities = invoiceMapper.toEntityList(response.body().getFacturas());
                            invoiceDao.insertAll(entities);
                            if (callback != null) callback.onSuccess(true);
                        } catch (Throwable e) {
                            if (callback != null) callback.onError(e);
                        }
                    });
                } else {
                    if (callback != null) callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                if (callback != null) callback.onError(t);
            }
        });
    }
}
