package com.example.pruebas;

import static com.example.pruebas.Invoice.stringToDate;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pruebas.databinding.ActivityInvoiceListBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class InvoiceListActivity extends AppCompatActivity {

    private ActivityInvoiceListBinding bindingInvoiceList;
    private InvoiceAdapter adapter;
    private InvoiceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuración de ViewBinding
        bindingInvoiceList = ActivityInvoiceListBinding.inflate(getLayoutInflater());
        setContentView(bindingInvoiceList.getRoot());

        // Configurar RecyclerView
        adapter = new InvoiceAdapter();
        bindingInvoiceList.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingInvoiceList.recyclerView.setAdapter(adapter);

        // Obtener el valor de USE_RETROMOCK desde la intención
        boolean useMock = getIntent().getBooleanExtra("USE_RETROMOCK", false);

        // Configurar ViewModel usando la Factory
        InvoiceViewModelFactory factory = new InvoiceViewModelFactory(useMock, this);
        viewModel = new ViewModelProvider(this, factory).get(InvoiceViewModel.class);

        // Cargar facturas
        viewModel.cargarFacturas();

        // Observar los datos de facturas y actualizar la UI
        viewModel.getFacturas().observe(this, facturas -> {
            if (facturas != null) {
                adapter.setFacturas(facturas);
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
            mostrarFiltroFragment(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Mostrar el fragmento de filtro
    private void mostrarFiltroFragment(MenuItem item) {
        // Obtener el importe máximo de las facturas
        float maxImporte = viewModel.getMaxImporte();

        // Crear el Bundle para pasar al fragmento
        Bundle bundle = new Bundle();
        bundle.putFloat("MAX_IMPORTE", maxImporte);

        // Mostrar el fragmento en toda la pantalla
        FilterFragment filterFragment = new FilterFragment();
        filterFragment.setArguments(bundle);

        bindingInvoiceList.fragmentContainer.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, filterFragment, "FILTRO_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Restaurar la vista principal
    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) != null) {
            restoreMainView();
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void aplicarFiltros(Bundle bundle) {
        // Recuperar los filtros desde el Bundle
        List<String> estadosSeleccionados = bundle.getStringArrayList("ESTADOS");
        String fechaInicio = bundle.getString("FECHA_INICIO");
        String fechaFin = bundle.getString("FECHA_FIN");
        Double importeMin = bundle.getDouble("IMPORTE_MIN");
        Double importeMax = bundle.getDouble("IMPORTE_MAX");

        // Filtrar las facturas
        List<Invoice> facturasFiltradas = filtrarFacturas(estadosSeleccionados, fechaInicio, fechaFin, importeMin, importeMax);

        // Mostrar las facturas filtradas si se encontró algún resultado
        if (facturasFiltradas != null && !facturasFiltradas.isEmpty()) {
            runOnUiThread(() -> adapter.setFacturas(facturasFiltradas));
        } else {
            Toast.makeText(InvoiceListActivity.this, "No se encontraron resultados", Toast.LENGTH_SHORT).show();
        }
    }

    public List<Invoice> filtrarFacturas(List<String> estadosSeleccionados, String fechaInicioString, String fechaFinString, Double importeMin, Double importeMax) {
        List<Invoice> facturasFiltradas = new ArrayList<>();

        // Obtener las facturas cargadas desde el ViewModel
        List<Invoice> facturas = viewModel.getFacturas().getValue();

        if (facturas == null) {
            return facturasFiltradas;  // Si no hay facturas, retornamos una lista vacía
        }

        // Convertir las fechas de String a Date para poder compararlas
        Date fechaInicio = stringToDate(fechaInicioString);
        Date fechaFin = stringToDate(fechaFinString);

        // Filtrar por estado
        for (Invoice factura : facturas) {
            boolean cumpleEstado = (estadosSeleccionados == null || estadosSeleccionados.contains(factura.getDescEstado()));

            // Filtrar por fecha
            boolean cumpleFecha = true;

            if (fechaInicio != null && factura.getFecha() != null) {
                cumpleFecha &= Objects.requireNonNull(stringToDate(factura.getFecha())).compareTo(fechaInicio) >= 0;  // Verificar si la factura es posterior o igual a la fecha de inicio
            }

            if (fechaFin != null && factura.getFecha() != null) {
                cumpleFecha &= Objects.requireNonNull(stringToDate(factura.getFecha())).compareTo(fechaFin) <= 0;  // Verificar si la factura es anterior o igual a la fecha de fin
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
}
