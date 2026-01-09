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

import java.time.LocalDate;

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
    public List<Invoice> filtrarFacturas(List<String> estadosSeleccionados,
                                         LocalDate fechaInicio,
                                         LocalDate fechaFin,
                                         Double importeMin,
                                         Double importeMax) {

        // Si no hay datos originales, limpiar y retornar vacío
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            facturas.setValue(new ArrayList<>());
            return new ArrayList<>();
        }

        List<Invoice> facturasFiltradas = new ArrayList<>();

        for (Invoice factura : facturasOriginales) {
            // 1. Filtrar por estado
            boolean cumpleEstado = (estadosSeleccionados == null ||
                    estadosSeleccionados.contains(factura.getDescEstado()));

            // 2. Filtrar por fecha (Optimizado)
            boolean cumpleFecha = true;
            LocalDate fechaFactura = factura.getFecha(); // Ya es un objeto, no hay parsing

            if (fechaFactura != null) {
                if (fechaInicio != null) {

                    cumpleFecha &= !fechaFactura.isBefore(fechaInicio);
                }
                if (fechaFin != null) {
                    // Optimizado: fechaFactura no debe ser después de fechaFin
                    cumpleFecha &= !fechaFactura.isAfter(fechaFin);
                }
            } else {
                // Si la factura no tiene fecha y hay filtro activo, descartar
                if (fechaInicio != null || fechaFin != null) {
                    cumpleFecha = false;
                }
            }

            // 3. Filtrar por importe
            boolean cumpleImporte = (importeMin == null || factura.getImporteOrdenacion() >= importeMin) &&
                    (importeMax == null || factura.getImporteOrdenacion() <= importeMax);

            // Si cumple todos los filtros, añadir
            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura);
            }
        }

        facturas.setValue(facturasFiltradas);
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
    public LocalDate getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return null;
        }

        LocalDate oldestDate = facturasOriginales.get(0).getFecha();

        for (Invoice factura : facturasOriginales) {
            LocalDate currentDate = factura.getFecha();
            if (currentDate != null && oldestDate != null && currentDate.isBefore(oldestDate)) {
                oldestDate = currentDate;
            }
        }
        return oldestDate;
    }


    /**
     * Devuelve true si ya hemos descargado facturas del servidor/mock,
     * independientemente de si el filtro actual las oculta todas.
     */
    public boolean hayDatosCargados() {
        return facturasOriginales != null && !facturasOriginales.isEmpty();
    }
}


