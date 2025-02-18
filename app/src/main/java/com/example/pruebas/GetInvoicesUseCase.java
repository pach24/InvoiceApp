package com.example.pruebas;


import com.example.pruebas.Invoice;
import com.example.pruebas.InvoiceRepository;
import com.example.pruebas.InvoiceResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class GetInvoicesUseCase {
    private final InvoiceRepository repository;

    public GetInvoicesUseCase(InvoiceRepository repository) {
        this.repository = repository;
    }

    public void execute(boolean useMock, Callback<List<Invoice>> callback) {
        repository.getFacturas(useMock).enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(null, Response.success(response.body().getFacturas()));
                } else {
                    callback.onFailure(null, new Throwable("Error en la respuesta de la API"));
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                callback.onFailure(null, t);
            }
        });
    }
}

