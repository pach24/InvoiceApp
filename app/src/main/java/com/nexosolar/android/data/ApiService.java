package com.nexosolar.android.data;

import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @Mock
    @MockResponse(body = "facturas.json") // Archivo en assets
    @GET("invoices.json") // Endpoint real de la API
    Call<InvoiceResponse> getFacturas();
}
