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
        // Al actualizar el LiveData en el ViewModel (sea por carga inicial o por filtro),
        // este observer actualizará la lista automáticamente.
        invoiceViewModel.getFacturas().observe(this, facturas -> {
            invalidateOptionsMenu(); // Actualiza estado del menú de filtros

            if (facturas != null) {
                // Si la lista está vacía (puede ser resultado de un filtro muy estricto)
                if (facturas.isEmpty()) {
                    // Opcional: mostrar un mensaje visual en lugar de Toast si prefieres
                    // Toast.makeText(InvoiceListActivity.this, "No hay facturas para mostrar", Toast.LENGTH_SHORT).show();
                }
                invoiceAdapter.setFacturas(facturas);
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

        // Verificamos si tenemos datos (usamos la lista actual del LiveData)
        boolean hayDatos = invoiceViewModel.getFacturas().getValue() != null &&
                !invoiceViewModel.getFacturas().getValue().isEmpty();

        filtroItem.setEnabled(hayDatos);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filters) {
            // Verificación extra antes de abrir
            if (invoiceViewModel.getFacturas().getValue() != null && !invoiceViewModel.getFacturas().getValue().isEmpty()) {
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
        // Ocultar elementos principales mientras se muestra el filtro (opcional, depende de tu diseño)
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
        // Obtenemos máximos y fechas de la lista original guardada en el ViewModel
        float maxImporte = invoiceViewModel.getMaxImporte();
        String oldestDate = invoiceViewModel.getOldestDate();

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
        String fechaInicio = bundle.getString("FECHA_INICIO");
        String fechaFin = bundle.getString("FECHA_FIN");
        Double importeMin = bundle.getDouble("IMPORTE_MIN");
        Double importeMax = bundle.getDouble("IMPORTE_MAX");

        // Delegamos TODA la lógica al ViewModel
        List<Invoice> facturasFiltradas = invoiceViewModel.filtrarFacturas(
                estadosSeleccionados,
                fechaInicio,
                fechaFin,
                importeMin,
                importeMax
        );

        Log.d("InvoiceListActivity", "Filtros aplicados. Resultados: " + facturasFiltradas.size());

        // El adapter se actualizará automáticamente gracias al Observer en onCreate.
        // Solo devolvemos si hubo resultados para que el fragmento sepa qué hacer (cerrarse o mostrar aviso)
        return !facturasFiltradas.isEmpty();
    }

    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);
    }
}
