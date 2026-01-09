package com.example.pruebas;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.pruebas.Invoice.stringToDate;

public class InvoiceViewModel extends ViewModel {

    // LiveData que la UI observa. Contiene la lista actual (filtrada o completa)
    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();

    // Lista interna para preservar TODOS los datos descargados
    private List<Invoice> facturasOriginales = new ArrayList<>();

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

    /**
     * Carga las facturas desde el repositorio.
     * Al recibir los datos, guardamos una copia en facturasOriginales.
     */
    public void cargarFacturas() {
        getInvoicesUseCase.execute(useMock, new Callback<List<Invoice>>() {
            @Override
            public void onResponse(@NonNull Call<List<Invoice>> call, @NonNull Response<List<Invoice>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    facturasOriginales = new ArrayList<>(response.body()); // Guardamos copia original
                    facturas.setValue(facturasOriginales); // Emitimos datos iniciales
                } else {
                    facturasOriginales = new ArrayList<>();
                    facturas.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Invoice>> call, @NonNull Throwable t) {
                facturasOriginales = new ArrayList<>();
                facturas.setValue(null);
            }
        });
    }

    /**
     * Lógica de filtrado
     * Filtra sobre facturasOriginales y actualiza el LiveData 'facturas'.
     */
    public List<Invoice> filtrarFacturas(List<String> estadosSeleccionados, String fechaInicioString, String fechaFinString, Double importeMin, Double importeMax) {
        // Si no hay datos originales, limpiar y retornar vacío
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            facturas.setValue(new ArrayList<>());
            return new ArrayList<>();
        }

        List<Invoice> facturasFiltradas = new ArrayList<>();
        Date fechaInicio = stringToDate(fechaInicioString);
        Date fechaFin = stringToDate(fechaFinString);

        for (Invoice factura : facturasOriginales) {
            // Filtrar por estado
            boolean cumpleEstado = (estadosSeleccionados == null || estadosSeleccionados.contains(factura.getDescEstado()));

            // Filtrar por fecha
            boolean cumpleFecha = true;
            Date fechaFactura = stringToDate(factura.getFecha());

            if (fechaInicio != null && fechaFactura != null) {
                cumpleFecha &= fechaFactura.compareTo(fechaInicio) >= 0;
            }
            if (fechaFin != null && fechaFactura != null) {
                cumpleFecha &= fechaFactura.compareTo(fechaFin) <= 0;
            }

            // Filtrar por importe
            boolean cumpleImporte = (importeMin == null || factura.getImporteOrdenacion() >= importeMin) &&
                    (importeMax == null || factura.getImporteOrdenacion() <= importeMax);

            // Si cumple todos los filtros, añadir
            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura);
            }
        }


        if (!facturasFiltradas.isEmpty()) {
            // Solo actualizamos la pantalla si encontramos algo
            facturas.setValue(facturasFiltradas);
        }


        return facturasFiltradas;
    }

    /**
     * Calcula el importe máximo basándose en la lista ORIGINAL completa.
     */
    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return 0f;
        }

        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            if (factura.getImporteOrdenacion() > maxImporte) {
                maxImporte = factura.getImporteOrdenacion();
            }
        }
        return maxImporte;
    }

    /**
     * Obtiene la fecha más antigua basándose en la lista ORIGINAL completa.
     */
    public String getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return null;
        }

        String oldestDate = facturasOriginales.get(0).getFecha();
        for (Invoice factura : facturasOriginales) {
            String currentDate = factura.getFecha();
            if (isEarlier(currentDate, oldestDate)) {
                oldestDate = currentDate;
            }
        }
        return oldestDate;
    }

    private boolean isEarlier(String date1, String date2) {
        if (date1 == null || date2 == null) return false;

        String[] parts1 = date1.split("/");
        String[] parts2 = date2.split("/");

        if (parts1.length != 3 || parts2.length != 3) return false;

        try {
            int day1 = Integer.parseInt(parts1[0]);
            int month1 = Integer.parseInt(parts1[1]);
            int year1 = Integer.parseInt(parts1[2]);

            int day2 = Integer.parseInt(parts2[0]);
            int month2 = Integer.parseInt(parts2[1]);
            int year2 = Integer.parseInt(parts2[2]);

            if (year1 != year2) return year1 < year2;
            if (month1 != month2) return month1 < month2;
            return day1 < day2;

        } catch (NumberFormatException e) {
            Log.e("InvoiceViewModel", "Error al convertir fecha");
            return false;
        }
    }

    /**
     * Devuelve true si ya hemos descargado facturas del servidor/mock,
     * independientemente de si el filtro actual las oculta todas.
     */
    public boolean hayDatosCargados() {
        return facturasOriginales != null && !facturasOriginales.isEmpty();
    }
}


