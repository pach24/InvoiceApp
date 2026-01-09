package com.example.pruebas;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

        // Configuración de la Toolbar
        setSupportActionBar(bindingInvoiceList.toolbar);

        // Configurar RecyclerView
        invoiceAdapter = new InvoiceAdapter();
        bindingInvoiceList.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingInvoiceList.recyclerView.setAdapter(invoiceAdapter);

        // Obtener el valor de USE_RETROMOCK desde el intent
        boolean useMock = getIntent().getBooleanExtra("USE_RETROMOCK", false);

        // Configurar ViewModel usando la Factory
        InvoiceViewModelFactory invoiceViewModelFactory = new InvoiceViewModelFactory(useMock, this);
        invoiceViewModel = new ViewModelProvider(this, invoiceViewModelFactory).get(InvoiceViewModel.class);

        // Cargar facturas iniciales
        invoiceViewModel.cargarFacturas();

        // --- OBSERVER PRINCIPAL ---
        invoiceViewModel.getFacturas().observe(this, facturas -> {
            invalidateOptionsMenu(); // Actualiza estado del menú de filtros

            if (facturas != null) {
                // Pasamos siempre la lista al adaptador, esté vacía o llena
                invoiceAdapter.setFacturas(facturas);

                // Lógica para mostrar/ocultar el "Empty State"
                if (facturas.isEmpty()) {
                    bindingInvoiceList.recyclerView.setVisibility(View.GONE);
                    bindingInvoiceList.layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
                    bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);
                }

            } else {
                Toast.makeText(InvoiceListActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para volver a la actividad principal
        bindingInvoiceList.btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);

        // Deshabilitar el ítem de filtro si no hay datos cargados en el ViewModel
        MenuItem filtroItem = menu.findItem(R.id.action_filters);

        if (invoiceViewModel.hayDatosCargados()) {
            filtroItem.setEnabled(true);
        } else {
            filtroItem.setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filters) {
            if (invoiceViewModel.hayDatosCargados()) {
                mostrarFiltroFragment();
            } else {
                Toast.makeText(this, "Las facturas aún no están cargadas.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarFiltroFragment() {
        FilterFragment filterFragment = getFilterFragment();

        bindingInvoiceList.fragmentContainer.setVisibility(View.VISIBLE);
        // Ocultar elementos principales mientras se muestra el filtro
        bindingInvoiceList.toolbar.setVisibility(View.GONE);
        bindingInvoiceList.recyclerView.setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, filterFragment, "FILTRO_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @NonNull
    private FilterFragment getFilterFragment() {
        float maxImporte = invoiceViewModel.getMaxImporte();

        // Obtener LocalDate y convertir a String para el fragmento
        LocalDate oldestDateObj = invoiceViewModel.getOldestDate();
        String oldestDate = "";

        // Formateador seguro
        if (oldestDateObj != null) {
            oldestDate = oldestDateObj.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            // Valor por defecto si no hay fecha antigua (ej. hoy)
            oldestDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        Bundle bundle = new Bundle();
        bundle.putFloat("MAX_IMPORTE", maxImporte);
        bundle.putString("OLDEST_DATE", oldestDate);

        FilterFragment filterFragment = new FilterFragment();
        filterFragment.setArguments(bundle);
        return filterFragment;
    }

    /**
     * Método llamado desde FilterFragment para aplicar los filtros seleccionados.
     */
    public boolean aplicarFiltros(Bundle bundle) {
        List<String> estadosSeleccionados = bundle.getStringArrayList("ESTADOS");
        String fechaInicioStr = bundle.getString("FECHA_INICIO");
        String fechaFinStr = bundle.getString("FECHA_FIN");
        Double importeMin = bundle.getDouble("IMPORTE_MIN");
        Double importeMax = bundle.getDouble("IMPORTE_MAX");

        // Convertir String -> LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaInicio = null;
        LocalDate fechaFin = null;

        try {
            if (fechaInicioStr != null && !fechaInicioStr.isEmpty())
                fechaInicio = LocalDate.parse(fechaInicioStr, formatter);

            if (fechaFinStr != null && !fechaFinStr.isEmpty())
                fechaFin = LocalDate.parse(fechaFinStr, formatter);

        } catch (DateTimeParseException e) {
            Log.e("InvoiceListActivity", "Error al parsear fechas de filtro", e);
        }

        // Llamar al ViewModel con objetos LocalDate
        invoiceViewModel.filtrarFacturas(
                estadosSeleccionados,
                fechaInicio,
                fechaFin,
                importeMin,
                importeMax
        );

        // Devolvemos true para indicar que se intentó aplicar el filtro
        // y que el fragmento debe cerrarse
        return true;
    }

    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);
    }
}
