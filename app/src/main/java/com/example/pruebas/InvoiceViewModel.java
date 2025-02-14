package com.example.pruebas;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;




import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

import android.util.Log;




/*Nuestro modelo de factura, ayuda a separar funciones. Nuestra actividad sólo se encarga de mostrar los datos
y el modelo de obtenerlos a través de RetroFit.*/
public class InvoiceViewModel extends ViewModel {

        private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>(); // contenedor de datos observable
        private final ApiService apiService;    // creamos la interfaz la cual permite hacer peticiones

         private final boolean useMock;
        public InvoiceViewModel(boolean useMock, Context context) {             // el constructor del modelo usará la api de Retrofit
            this.useMock = useMock;
            if (useMock) {
                apiService = RetromockClient.getClient(context).create(ApiService.class);
                Log.d("InvoiceViewModel", "Cargando facturas desde Retromock.");
            } else {
                apiService = RetroFitClient.getClient(context).create(ApiService.class);
                Log.d("InvoiceViewModel", "Cargando facturas desde Retrofit.");
            }
        }

        public LiveData<List<Invoice>> getFacturas() {  //Metodo para expone los datos usando LiveData
            return facturas;
        }

        //Obtenemos los datos cargados en InvoiceResponse
        public void cargarFacturas() {
            // Llamamos al metodo de la interfaz para cargar facturas,
            // usa enqueue() para hacer la petición en segundo plano y permite el uso de Callback
            // que tiene los métodos onResponse y onFailure

            if (useMock) {
                // Usar la versión de Retromock
                apiService.getMockFacturas().enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<InvoiceResponse> call, @NonNull Response<InvoiceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Invoice> facturasList = response.body().getFacturas();
                            Log.d("InvoiceViewModel", "Facturas recibidas desde Retromock: " + facturasList.size());
                            facturas.setValue(facturasList);
                        } else {
                            Log.e("InvoiceViewModel", "Respuesta fallida o sin cuerpo desde Retromock.");
                            facturas.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<InvoiceResponse> call, @NonNull Throwable t) {
                        Log.e("InvoiceViewModel", "Error en la API de Retromock: " + t.getMessage());
                        facturas.setValue(null);
                    }
                });
            } else {
                // Usar la versión de Retrofit
                apiService.getFacturas().enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<InvoiceResponse> call, @NonNull Response<InvoiceResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Invoice> facturasList = response.body().getFacturas();
                            Log.d("InvoiceViewModel", "Facturas recibidas desde Retrofit: " + facturasList.size());
                            facturas.setValue(facturasList);
                        } else {
                            Log.e("InvoiceViewModel", "Respuesta fallida o sin cuerpo desde Retrofit.");
                            facturas.setValue(null);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<InvoiceResponse> call, @NonNull Throwable t) {
                        Log.e("InvoiceViewModel", "Error en la API de Retrofit: " + t.getMessage());
                        facturas.setValue(null);
                    }
                });
            }
        }

}





