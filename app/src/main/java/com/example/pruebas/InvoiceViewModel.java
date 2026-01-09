package com.example.pruebas;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.time.LocalDate;

public class InvoiceViewModel extends ViewModel {

    // LiveData que la UI observa
    private final MutableLiveData<List<Invoice>> facturas = new MutableLiveData<>();

    // Lista interna para preservar TODOS los datos
    private List<Invoice> facturasOriginales = new ArrayList<>();

    private final GetInvoicesUseCase getInvoicesUseCase;

    // ELIMINADO: private final boolean useMock; (Ya no hace falta guardarlo en la clase)

    public InvoiceViewModel(boolean useMock, Context context) {
        // ELIMINADO: this.useMock = useMock;

        // Usamos el booleano solo aquí para crear el repositorio correcto
        InvoiceRepository repository = new InvoiceRepository(useMock, context);
        this.getInvoicesUseCase = new GetInvoicesUseCase(repository);
    }

    public LiveData<List<Invoice>> getFacturas() {
        return facturas;
    }

    public void cargarFacturas() {
        // Correcto: ya no pasamos parámetros aquí
        getInvoicesUseCase.execute(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(@NonNull Call<InvoiceResponse> call, @NonNull Response<InvoiceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    facturasOriginales = new ArrayList<>(response.body().getFacturas());
                    facturas.setValue(facturasOriginales);
                } else {
                    manejarError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<InvoiceResponse> call, @NonNull Throwable t) {
                manejarError();
            }
        });
    }

    private void manejarError() {
        facturasOriginales = new ArrayList<>();
        facturas.setValue(null);
    }

    public List<Invoice> filtrarFacturas(List<String> estadosSeleccionados,
                                         LocalDate fechaInicio,
                                         LocalDate fechaFin,
                                         Double importeMin,
                                         Double importeMax) {

        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            facturas.setValue(new ArrayList<>());
            return new ArrayList<>();
        }

        List<Invoice> facturasFiltradas = new ArrayList<>();

        for (Invoice factura : facturasOriginales) {
            // 1. Filtrar por estado
            boolean cumpleEstado = (estadosSeleccionados == null ||
                    estadosSeleccionados.contains(factura.getDescEstado()));

            // 2. Filtrar por fecha
            boolean cumpleFecha = true;
            LocalDate fechaFactura = factura.getFecha();

            if (fechaFactura != null) {
                if (fechaInicio != null) {
                    cumpleFecha &= !fechaFactura.isBefore(fechaInicio);
                }
                if (fechaFin != null) {
                    cumpleFecha &= !fechaFactura.isAfter(fechaFin);
                }
            } else {
                if (fechaInicio != null || fechaFin != null) {
                    cumpleFecha = false;
                }
            }

            // 3. Filtrar por importe
            boolean cumpleImporte = (importeMin == null || factura.getImporteOrdenacion() >= importeMin) &&
                    (importeMax == null || factura.getImporteOrdenacion() <= importeMax);

            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura);
            }
        }

        facturas.setValue(facturasFiltradas);
        return facturasFiltradas;
    }

    public float getMaxImporte() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return 0f;
        }
        float maxImporte = 0f;
        for (Invoice factura : facturasOriginales) {
            // Protección contra null pointer si importeOrdenacion fuera objeto (aunque es float primitivo aquí)
            if (factura.getImporteOrdenacion() > maxImporte) {
                maxImporte = factura.getImporteOrdenacion();
            }
        }
        return maxImporte;
    }

    /**
     * CORREGIDO: Lógica más robusta para encontrar la fecha más antigua.
     */
    public LocalDate getOldestDate() {
        if (facturasOriginales == null || facturasOriginales.isEmpty()) {
            return null;
        }

        LocalDate oldestDate = null;

        for (Invoice factura : facturasOriginales) {
            LocalDate currentDate = factura.getFecha();

            // Si la factura tiene fecha...
            if (currentDate != null) {
                // Si aún no tenemos fecha antigua guardada, o la actual es anterior a la guardada...
                if (oldestDate == null || currentDate.isBefore(oldestDate)) {
                    oldestDate = currentDate;
                }
            }
        }
        return oldestDate;
    }

    public boolean hayDatosCargados() {
        return facturasOriginales != null && !facturasOriginales.isEmpty();
    }
}
