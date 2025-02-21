package com.example.pruebas;

import static com.example.pruebas.Invoice.stringToDate;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.pruebas.databinding.ActivityInvoiceListBinding;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class InvoiceListActivity extends AppCompatActivity {

    private ActivityInvoiceListBinding bindingInvoiceList;
    private InvoiceAdapter invoiceAdapter;
    private InvoiceViewModel invoiceViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de ViewBinding
        bindingInvoiceList = ActivityInvoiceListBinding.inflate(getLayoutInflater());
        setContentView(bindingInvoiceList.getRoot());

        // Configurar RecyclerView
        invoiceAdapter = new InvoiceAdapter();
        bindingInvoiceList.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingInvoiceList.recyclerView.setAdapter(invoiceAdapter);

        // Obtener el valor de USE_RETROMOCK desde el intent
        boolean useMock = getIntent().getBooleanExtra("USE_RETROMOCK", false);

        // Configurar ViewModel usando la Factory
        InvoiceViewModelFactory invoiceViewModelFactory = new InvoiceViewModelFactory(useMock, this);
        invoiceViewModel = new ViewModelProvider(this, invoiceViewModelFactory).get(InvoiceViewModel.class);

        // Cargar facturas desde el viewmodel
        invoiceViewModel.cargarFacturas();

        // Observar los datos de facturas y actualizar la UI
        invoiceViewModel.getFacturas().observe(this, facturas -> {
            if (facturas != null) {
                invoiceAdapter.setFacturas(facturas);

                for (Invoice factura : facturas) {
                    Log.d("InvoiceListActivity", "Factura recibida - Estado: " + factura.getDescEstado());
                }

            } else {
                Toast.makeText(InvoiceListActivity.this, "No se encontraron facturas", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para volver a la actividad principal
        bindingInvoiceList.btnVolver.setOnClickListener(v -> finish());

        // Configuración de la Toolbar
        setSupportActionBar(bindingInvoiceList.toolbar);
    }

    // Inflar el menú
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }

    // Gestionar la acción de seleccionar el ítem del menú
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filters) {
            mostrarFiltroFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Mostrar el fragmento de filtro
    private void mostrarFiltroFragment() {
        // Obtener el importe máximo de las facturas
        FilterFragment filterFragment = getFilterFragment();

        bindingInvoiceList.fragmentContainer.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, filterFragment, "FILTRO_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @NonNull
    private FilterFragment getFilterFragment() {
        float maxImporte = invoiceViewModel.getMaxImporte();
        String oldestDate = invoiceViewModel.getOldestDate();

        // Crear el Bundle para pasar al fragmento
        Bundle bundle = new Bundle();

        // Pasamos el importe máximo y la fecha más antigua
        bundle.putFloat("MAX_IMPORTE", maxImporte);
        bundle.putString("OLDEST_DATE", oldestDate);


        // Mostrar el fragmento en toda la pantalla
        FilterFragment filterFragment = new FilterFragment();
        filterFragment.setArguments(bundle);
        return filterFragment;
    }

    public List<Invoice> filtrarFacturas(List<String> estadosSeleccionados, String fechaInicioString, String fechaFinString, Double importeMin, Double importeMax) {
        List<Invoice> facturasFiltradas = new ArrayList<>();

        // Obtener las facturas cargadas desde el ViewModel
        List<Invoice> facturas = invoiceViewModel.getFacturas().getValue();

        if (facturas == null) {
            return facturasFiltradas;  // Si no hay facturas, retornamos una lista vacía
        }

        // Convertir las fechas de String a Date una sola vez
        Date fechaInicio = stringToDate(fechaInicioString);
        Date fechaFin = stringToDate(fechaFinString);

        // Filtrar por estado, fecha e importe
        for (Invoice factura : facturas) {
            boolean cumpleEstado = (estadosSeleccionados == null || estadosSeleccionados.contains(factura.getDescEstado()));

            // Filtrar por fecha
            boolean cumpleFecha = true;
            Date fechaFactura = stringToDate(factura.getFecha());

            if (fechaInicio != null && fechaFactura != null) {
                cumpleFecha &= fechaFactura.compareTo(fechaInicio) >= 0;  // Verificar si la factura es posterior o igual a la fecha de inicio
            }

            if (fechaFin != null && fechaFactura != null) {
                cumpleFecha &= fechaFactura.compareTo(fechaFin) <= 0;  // Verificar si la factura es anterior o igual a la fecha de fin
            }

            // Filtrar por importe
            boolean cumpleImporte = (importeMin == null || factura.getImporteOrdenacion() >= importeMin) &&
                    (importeMax == null || factura.getImporteOrdenacion() <= importeMax);

            // Si cumple todos los filtros, añadir la factura a la lista filtrada
            if (cumpleEstado && cumpleFecha && cumpleImporte) {
                facturasFiltradas.add(factura);
            }
        }

        return facturasFiltradas;
    }




    // Recupera los datos aplicados en filtros
    public void aplicarFiltros(Bundle bundle) {

        List<String> estadosSeleccionados = bundle.getStringArrayList("ESTADOS");
        String fechaInicio = bundle.getString("FECHA_INICIO");
        String fechaFin = bundle.getString("FECHA_FIN");
        Double importeMin = bundle.getDouble("IMPORTE_MIN");
        Double importeMax = bundle.getDouble("IMPORTE_MAX");


        // Filtrar las facturas
        List<Invoice> facturasFiltradas = filtrarFacturas(estadosSeleccionados, fechaInicio, fechaFin, importeMin, importeMax);
        Log.d("InvoiceListActivity", "Facturas filtradas: " + facturasFiltradas.size());
        for (Invoice factura : facturasFiltradas) {
            Log.d("InvoiceListActivity", "Estado de factura: " + factura.getDescEstado());
        }

        // Mostrar las facturas filtradas si se encontró algún resultado
        if (!facturasFiltradas.isEmpty()) {
            runOnUiThread(() -> invoiceAdapter.setFacturas(facturasFiltradas));
        } else {
            Toast.makeText(InvoiceListActivity.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
        }
    }

    // Restaurar la vista principal
    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);
    }


}
