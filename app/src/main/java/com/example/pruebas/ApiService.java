package com.example.pruebas;


import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockResponse;
import retrofit2.Call;
import retrofit2.http.GET;

    //Define las peticiones que hacemos a la API, en nuestro caso GET
    public interface ApiService {
        @GET("facturas")
        Call<InvoiceResponse> getFacturas();

        @Mock
        @MockResponse(body = "facturas.json") // Ruta del archivo en assets
        @GET("/")
        Call<InvoiceResponse> getMockFacturas();
    }



