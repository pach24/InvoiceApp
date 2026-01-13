package com.nexosolar.android.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.nexosolar.android.data.local.AppDatabase;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.domain.Invoice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRepository {

    private final ApiService apiService;
    private final InvoiceDao invoiceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // Para escribir en background

    public InvoiceRepository(boolean useMock, Context context) {
        // 1. Inicializar API (Tu código existente)
        if (useMock) {
            this.apiService = RetromockClient.getClient(context).create(ApiService.class);
        } else {
            this.apiService = RetroFitClient.getClient(context).create(ApiService.class);
        }

        // 2. Inicializar Room
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
    }

    // A. FUENTE DE LA VERDAD: Solo lee de Room
    public LiveData<List<Invoice>> getFacturas() {
        return invoiceDao.getAll();
    }

    // B. ACTUALIZACIÓN: Llama a API -> Guarda en Room
    public void refreshFacturas() {
        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Guardamos en un hilo secundario para no bloquear la UI
                    executor.execute(() -> {
                        invoiceDao.deleteAll(); // Limpiamos caché vieja
                        invoiceDao.insertAll(response.body().getFacturas()); // Guardamos nueva
                    });
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                // Aquí podrías guardar un error en un LiveData de "Status" si quisieras
            }
        });
    }
}
