package com.nexosolar.android.data.repository;

import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.domain.models.Installation;
import com.nexosolar.android.domain.repository.InstallationRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementación del repositorio para gestionar datos de instalaciones solares.
 *
 * Actúa como intermediario entre la capa de dominio y la fuente de datos remota (API).
 * Solo maneja datos remotos ya que la información de instalaciones se consulta
 * bajo demanda y no requiere persistencia local.
 */
public class InstallationRepositoryImpl implements InstallationRepository {

    // ===== Variables de instancia =====

    private final ApiService apiService;

    // ===== Constructores =====

    public InstallationRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

    // ===== Métodos públicos =====

    /**
     * Obtiene los detalles de la instalación desde la API remota.
     *
     * Realiza una llamada asíncrona a la API y notifica el resultado mediante callback.
     *
     * @param callback Callback para notificar el resultado de la operación
     */
    @Override
    public void getInstallationDetails(InstallationCallback callback) {
        apiService.getInstallationDetails().enqueue(new Callback<Installation>() {
            @Override
            public void onResponse(Call<Installation> call, Response<Installation> response) {
                if (response.isSuccessful() && response.body() != null) {
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
