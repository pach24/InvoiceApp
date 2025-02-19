package com.example.pruebas;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InvoiceViewModel extends ViewModel {
    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();
    private final GetInvoicesUseCase getInvoicesUseCase;
    private final boolean useMock;

    public InvoiceViewModel(boolean useMock, Context context) {
        this.useMock = useMock;
        InvoiceRepository repository = new InvoiceRepository(useMock, context);
        this.getInvoicesUseCase = new GetInvoicesUseCase(repository);
    }

    public LiveData<List<Invoice>> getFacturas() {
        return facturas;
    }

    public void cargarFacturas() {
        getInvoicesUseCase.execute(useMock, new Callback<List<Invoice>>() {
            @Override
            public void onResponse(@NonNull Call<List<Invoice>> call, @NonNull Response<List<Invoice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    facturas.setValue(response.body());
                    Log.d("InvoiceViewModel", "Facturas recibidas: " + response.body().size());
                } else {
                    facturas.setValue(null);
                    Log.e("InvoiceViewModel", "Error al obtener las facturas.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Invoice>> call, @NonNull Throwable t) {
                facturas.setValue(null);
                Log.e("InvoiceViewModel", "Error en la API: " + t.getMessage());
            }
        });
    }
    public float getMaxImporte() {
        if (facturas.getValue() == null || facturas.getValue().isEmpty()) {
            return 0f;
        }

        float maxImporte = 0f;
        for (Invoice factura : facturas.getValue()) {
            if (factura.getImporteOrdenacion() > maxImporte) {
                maxImporte = (float) factura.getImporteOrdenacion();
            }
        }
        return maxImporte;
    }
}
