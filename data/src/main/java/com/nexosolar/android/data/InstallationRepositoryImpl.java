package com.nexosolar.android.data;

import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.domain.Installation;
import com.nexosolar.android.domain.InstallationRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementación del repositorio en la capa DATA.
 * Coordina la obtención de datos (en este caso, solo de red).
 */
public class InstallationRepositoryImpl implements InstallationRepository {

    private final ApiService apiService;

    // Inyección de dependencias (Manual o Hilt)
    public InstallationRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void getInstallationDetails(InstallationCallback callback) {
        apiService.getInstallationDetails().enqueue(new Callback<Installation>() {
            @Override
            public void onResponse(Call<Installation> call, Response<Installation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Mapeo directo: Modelo de API -> Modelo de Dominio
                    // (Si tuvieras mappers, aquí los usarías)
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Installation> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}
