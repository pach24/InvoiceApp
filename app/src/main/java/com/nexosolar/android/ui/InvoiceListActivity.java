package com.nexosolar.android.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nexosolar.android.R;
import com.nexosolar.android.databinding.ActivityInvoiceListBinding;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
        bindingInvoiceList.shimmerViewContainer.startShimmer();

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

            bindingInvoiceList.shimmerViewContainer.stopShimmer();
            bindingInvoiceList.shimmerViewContainer.setVisibility(View.GONE);

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
    // Método auxiliar para obtener el fragmento
    private FilterFragment getFilterFragment() {
        float maxImporte = invoiceViewModel.getMaxImporte();
        LocalDate oldestDateObj = invoiceViewModel.getOldestDate();

        Bundle bundle = new Bundle();
        bundle.putFloat("MAX_IMPORTE", maxImporte);

        // PASO CLAVE: Convertir LocalDate a long para pasarlo al fragmento
        if (oldestDateObj != null) {
            long millis = oldestDateObj.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            bundle.putLong("OLDEST_DATE_MILLIS", millis);
        }

        FilterFragment filterFragment = new FilterFragment();
        filterFragment.setArguments(bundle);
        return filterFragment;
    }

    /**
     * Método llamado desde FilterFragment para aplicar los filtros seleccionados.
     */
    public boolean aplicarFiltros(Bundle bundle) {
        List<String> estadosSeleccionados = bundle.getStringArrayList("ESTADOS");
        Double importeMin = bundle.getDouble("IMPORTE_MIN");
        Double importeMax = bundle.getDouble("IMPORTE_MAX");

        // PASO CLAVE: Recuperar Long y convertir a LocalDate
        // getLong devuelve 0 si no existe la key, por eso verificamos containsKey o usamos un valor centinela (-1)
        LocalDate fechaInicio = null;
        if (bundle.containsKey("FECHA_INICIO_MILLIS")) {
            long millis = bundle.getLong("FECHA_INICIO_MILLIS");
            fechaInicio = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
        }

        LocalDate fechaFin = null;
        if (bundle.containsKey("FECHA_FIN_MILLIS")) {
            long millis = bundle.getLong("FECHA_FIN_MILLIS");
            fechaFin = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
        }

        // Ya tenemos LocalDate puros, sin try-catch ni parseo de Strings
        invoiceViewModel.filtrarFacturas(
                estadosSeleccionados,
                fechaInicio,
                fechaFin,
                importeMin,
                importeMax
        );

        restoreMainView();
        return true;
    }

    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);
    }
}
