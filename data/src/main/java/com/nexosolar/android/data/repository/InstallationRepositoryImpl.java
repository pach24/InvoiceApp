package com.nexosolar.android.data.repository;

import com.nexosolar.android.data.InstallationMapper;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.remote.InstallationDTO;
import com.nexosolar.android.domain.models.Installation;
import com.nexosolar.android.domain.repository.InstallationRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    private final ApiService apiService;
    private final ExecutorService executor;

    public InstallationRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Obtiene los detalles de la instalación desde la API remota.
     *
     * @param callback Callback para notificar el resultado de la operación
     */
    @Override
    public void getInstallationDetails(InstallationCallback callback) {
        apiService.getInstallationDetails().enqueue(new Callback<InstallationDTO>() {
            @Override
            public void onResponse(Call<InstallationDTO> call, Response<InstallationDTO> response) {
                executor.execute(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Installation installation = InstallationMapper.toDomain(response.body());
                        callback.onSuccess(installation);
                    } else {
                        callback.onError("Error del servidor: " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(Call<InstallationDTO> call, Throwable t) {
                executor.execute(() -> callback.onError("Error de conexión: " + t.getMessage()));
            }
        });
    }
}
