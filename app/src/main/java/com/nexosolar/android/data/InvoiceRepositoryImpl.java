package com.nexosolar.android.data;

import android.content.Context;
import com.nexosolar.android.data.local.AppDatabase;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.remote.InvoiceResponse;
import com.nexosolar.android.data.remote.RetroFitClient;
import com.nexosolar.android.data.remote.RetromockClient;
import com.nexosolar.android.domain.Invoice;
import com.nexosolar.android.domain.InvoiceRepository;
import com.nexosolar.android.domain.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final ApiService apiService;
    private final InvoiceDao invoiceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public InvoiceRepositoryImpl(boolean useMock, Context context) {
        // Inicializar API o Mock según el flag
        if (useMock) {
            this.apiService = RetromockClient.getClient(context).create(ApiService.class);
        } else {
            this.apiService = RetroFitClient.getClient().create(ApiService.class);
        }

        // Inicializar siempre la base de datos local
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
    }

    @Override
    public void getFacturas(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            try {
                // 1. Obtener datos de la DB
                List<InvoiceEntity> entities = invoiceDao.getAllList();

                // 2. Mapear Entity -> Domain
                List<Invoice> domainList = new ArrayList<>();
                if (entities != null) {
                    for (InvoiceEntity entity : entities) {
                        domainList.add(entity.toDomain());
                    }
                }

                // 3. Devolver datos al callback
                callback.onSuccess(domainList);

            } catch (Throwable e) { // CAMBIO CLAVE: Usamos Throwable en vez de Exception
                e.printStackTrace(); // Imprimir el error real en el Logcat
                callback.onError(e); // Avisar al ViewModel para que oculte el "Loading..."
            }
        });
    }


    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Invoice> facturasApi = response.body().getFacturas();
                    executor.execute(() -> {
                        try {
                            // Mapear Domain (API) -> Entity (BD)
                            List<InvoiceEntity> entitiesToSave = new ArrayList<>();
                            for (Invoice inv : facturasApi) {
                                entitiesToSave.add(InvoiceEntity.fromDomain(inv));
                            }
                            // Reemplazar datos en BD
                            invoiceDao.deleteAll();
                            invoiceDao.insertAll(entitiesToSave);

                            if (callback != null) callback.onSuccess(true);

                        } catch (Throwable e) { // <--- ¡CAMBIO IMPORTANTE! Usar Throwable
                            e.printStackTrace();
                            if (callback != null) callback.onError(e);
                        }
                    });
                } else {
                    if (callback != null) callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                t.printStackTrace();
                if (callback != null) callback.onError(t);
            }
        });
    }



}
