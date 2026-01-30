package com.nexosolar.android.data.remote;

import com.nexosolar.android.domain.models.Installation;

import co.infinum.retromock.meta.Mock;
import co.infinum.retromock.meta.MockCircular;
import co.infinum.retromock.meta.MockResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Interfaz de definición de servicios API con soporte para Retrofit y Retromock.
 * Define los endpoints y sus respuestas simuladas.
 */
public interface ApiService {

    /**
     * Obtiene la lista de facturas.
     *
     * En modo Mock, utiliza @MockCircular para rotar entre diferentes respuestas JSON:
     * 1. facturas_todas_impagadas.json (Todas pendientes)
     * 2. facturas_algunas_pagadas.json (Mezcla pagadas/pendientes)
     * 3. facturas_todas_pagadas.json (Todas pagadas)
     *
     * En modo Real, llama al endpoint "facturas".
     *
     * @return Call con la respuesta de facturas
     */
    @Mock
    @MockCircular
    @MockResponse(body = "facturas_500.json")
    @MockResponse(body = "facturas_todas_impagadas.json")
    @MockResponse(body = "facturas_algunas_pagadas.json")
    @MockResponse(body = "facturas_todas_pagadas.json")
    @GET("invoices.json")
    Call<InvoiceResponse> getFacturas();




    /**
     * Obtiene los detalles de la instalación solar (Smart Solar).
     *
     * En modo Mock, devuelve el contenido de "installation_details.json".
     * En modo Real, llamaría a "installation/details".
     */
    @Mock
    @MockResponse(body = "installation_details.json")
    @GET("installation_details.json")
    Call<InstallationDTO> getInstallationDetails();
}
