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
        getInvoicesUseCase.execute(useMock, new Callback<>() {
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
                maxImporte = factura.getImporteOrdenacion();
            }
        }
        return maxImporte;
    }

    /*
    Metodo que devuelve la fecha más antigua de la lista de facturas.
    Compara las fechas con strings, lo óptimo es que las fechas fueran
    tipo date y compararlas de otra manera.
     */

    public String getOldestDate() {
        if (facturas.getValue() == null || facturas.getValue().isEmpty()) {
            return null;
        }

        String oldestDate = facturas.getValue().get(0).getFecha(); // Inicializa con la primera fecha

        for (Invoice factura : facturas.getValue()) {
            String currentDate = factura.getFecha();
            if (isEarlier(currentDate, oldestDate)) {
                oldestDate = currentDate; // Actualiza si encuentras una fecha más temprana
            }
        }

        return oldestDate; // Retorna la fecha más temprana
    }

    /*
    Metodo auxuilar para comparar fechas en nuestro tipo de formato (dd/mm/yyyy)
     */
    private boolean isEarlier(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        // Dividir la fecha en día, mes y año
        String[] parts1 = date1.split("/");
        String[] parts2 = date2.split("/");

        // Validar que ambas fechas tengan 3 partes
        if (parts1.length != 3 || parts2.length != 3) {
            return false;
        }

        try {
            int day1 = Integer.parseInt(parts1[0]);
            int month1 = Integer.parseInt(parts1[1]);
            int year1 = Integer.parseInt(parts1[2]);

            int day2 = Integer.parseInt(parts2[0]);
            int month2 = Integer.parseInt(parts2[1]);
            int year2 = Integer.parseInt(parts2[2]);

            // Comparar por año
            if (year1 != year2) {
                return year1 < year2;
            }
            // Si los años son iguales, comparar por mes
            if (month1 != month2) {
                return month1 < month2;
            }
            // Si los meses también son iguales, comparar por día
            return day1 < day2;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }



}
