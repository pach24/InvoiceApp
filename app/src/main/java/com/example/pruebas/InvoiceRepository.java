package com.example.pruebas;


import android.content.Context;
import retrofit2.Call;

public class InvoiceRepository {
    private final ApiService apiService;

    public InvoiceRepository(boolean useMock, Context context) {
        this.apiService = useMock
                ? RetromockClient.getClient(context).create(ApiService.class)
                : RetroFitClient.getClient(context).create(ApiService.class);
    }

    public Call<InvoiceResponse> getFacturas(boolean useMock) {
        return useMock ? apiService.getMockFacturas() : apiService.getFacturas();
    }
}
