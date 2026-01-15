package com.nexosolar.android.data.source;

import com.nexosolar.android.data.InvoiceMapper;
import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.remote.InvoiceResponse;
import com.nexosolar.android.domain.repository.RepositoryCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceRemoteDataSourceImpl implements InvoiceRemoteDataSource {

    private final ApiService apiService;
    private final InvoiceMapper mapper;

    public InvoiceRemoteDataSourceImpl(ApiService apiService) {
        this.apiService = apiService;
        this.mapper = new InvoiceMapper();
    }

    @Override
    public void getFacturas(RepositoryCallback<List<InvoiceEntity>> callback) {
        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convertimos Response -> Entity aquí
                    List<InvoiceEntity> entities = mapper.toEntityList(response.body().getFacturas());
                    callback.onSuccess(entities);
                } else {
                    callback.onError(new Exception("Error del servidor: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
}
