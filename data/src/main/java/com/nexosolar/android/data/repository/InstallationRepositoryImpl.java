package com.nexosolar.android.data.repository;

import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.domain.models.Installation;
import com.nexosolar.android.domain.repository.InstallationRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InstallationRepositoryImpl implements InstallationRepository {

    private final ApiService apiService;

    public InstallationRepositoryImpl(ApiService apiService) {
        this.apiService = apiService;
    }

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
