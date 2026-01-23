package com.nexosolar.android.data.source;

import android.util.Log;

import com.nexosolar.android.data.InvoiceMapper;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.remote.InvoiceResponse;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementación del origen de datos remoto para facturas usando Retrofit.
 *
 * Encapsula toda la lógica de comunicación con la API REST, incluyendo
 * el manejo de respuestas HTTP y el mapeo de DTOs (InvoiceResponse) a
 * entidades de base de datos (InvoiceEntity).
 *
 * Esta capa actúa como barrera entre el protocolo HTTP y el resto de la
 * aplicación, traduciendo errores de red en excepciones manejables.
 */
public class InvoiceRemoteDataSourceImpl implements InvoiceRemoteDataSource {

    // ===== Variables de instancia =====

    private final ApiService apiService;
    private final InvoiceMapper mapper;

    // ===== Constructores =====

    public InvoiceRemoteDataSourceImpl(ApiService apiService) {
        this.apiService = apiService;
        this.mapper = new InvoiceMapper();
    }




    // ===== Métodos públicos =====

    /**
     * Obtiene facturas desde la API y las transforma a entidades.
     *
     * Realiza la llamada HTTP asíncrona, valida la respuesta y mapea los DTOs
     * de la respuesta JSON a entidades de Room. Los errores HTTP se convierten
     * en excepciones con mensajes descriptivos.
     *
     * @param callback Callback para notificar el resultado de la operación
     */
    @Override
    public void getFacturas(RepositoryCallback<List<InvoiceEntity>> callback) {
        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<InvoiceEntity> entities = mapper.toEntityListFromDto(response.body().getFacturas());
                    callback.onSuccess(entities);
                } else {
                    callback.onError(new Exception("Error del servidor: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                Log.e("API_DEBUG", "Fallo total: " + t.getMessage()); // <--- MIRA ESTO EN LOGCAT
                t.printStackTrace();
                callback.onError(t);
            }

        });
    }

}
