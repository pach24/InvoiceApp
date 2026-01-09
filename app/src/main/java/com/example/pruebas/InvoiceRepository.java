package com.example.pruebas;

import android.content.Context;
import retrofit2.Call;

public class InvoiceRepository {

    private final ApiService apiService;

    public InvoiceRepository(boolean useMock, Context context) {
        // DECISIÓN ÚNICA: Aquí elegimos el cliente una sola vez al crear el repositorio
        if (useMock) {
            this.apiService = RetromockClient.getClient(context).create(ApiService.class);
        } else {
            this.apiService = RetroFitClient.getClient(context).create(ApiService.class);
        }
    }


    public Call<InvoiceResponse> getFacturas() {
        return apiService.getFacturas();
    }
}
