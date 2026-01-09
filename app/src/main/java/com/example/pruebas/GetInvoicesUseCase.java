package com.example.pruebas;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class GetInvoicesUseCase {

    private final InvoiceRepository repository;

    public GetInvoicesUseCase(InvoiceRepository repository) {
        this.repository = repository;
    }

    // Nota que el callback de vuelta a la UI espera List<Invoice>
    public void execute(boolean useMock, final Callback<List<Invoice>> callback) {

        // CORRECCIÓN: Especificar <InvoiceResponse> explícitamente en lugar de <>
        repository.getFacturas(useMock).enqueue(new Callback<InvoiceResponse>() {

            @Override
            public void onResponse(@NonNull Call<InvoiceResponse> call, @NonNull Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extraemos la lista de facturas de la respuesta (InvoiceResponse)
                    // y se la pasamos al callback de la UI
                    callback.onResponse(null, Response.success(response.body().getFacturas()));
                } else {
                    callback.onFailure(null, new Throwable("Error en la respuesta de la API"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<InvoiceResponse> call, @NonNull Throwable t) {
                callback.onFailure(null, t);
            }
        });
    }
}
