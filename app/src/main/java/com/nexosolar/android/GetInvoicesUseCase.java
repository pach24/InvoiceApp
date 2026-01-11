package com.nexosolar.android;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetInvoicesUseCase {

    private final InvoiceRepository repository;

    public GetInvoicesUseCase(InvoiceRepository repository) {
        this.repository = repository;
    }

    // ANTES: public void execute(boolean useMock, final Callback<InvoiceResponse> callback)
    // AHORA: Quitamos 'boolean useMock'
    public void execute(final Callback<InvoiceResponse> callback) {

        // Ya no pasamos 'useMock' al repositorio tampoco
        repository.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(@NonNull Call<InvoiceResponse> call, @NonNull Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(call, response);
                } else {
                    callback.onFailure(call, new Throwable("Error en la respuesta de la API"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<InvoiceResponse> call, @NonNull Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }
}
