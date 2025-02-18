package com.example.pruebas;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pruebas.Invoice;
import com.example.pruebas.InvoiceRepository;
import com.example.pruebas.GetInvoicesUseCase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import android.util.Log;

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
}







