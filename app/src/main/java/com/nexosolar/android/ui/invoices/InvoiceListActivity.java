package com.nexosolar.android.ui.invoices;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nexosolar.android.R;
import com.nexosolar.android.databinding.ActivityInvoiceListBinding;
import com.nexosolar.android.domain.models.Invoice;

import java.util.List;

public class InvoiceListActivity extends AppCompatActivity {

    private ActivityInvoiceListBinding bindingInvoiceList;
    private InvoiceAdapter invoiceAdapter;
    private InvoiceViewModel invoiceViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingInvoiceList = ActivityInvoiceListBinding.inflate(getLayoutInflater());
        setContentView(bindingInvoiceList.getRoot());
        setSupportActionBar(bindingInvoiceList.toolbar);

        // Configurar RecyclerView
        invoiceAdapter = new InvoiceAdapter();
        bindingInvoiceList.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingInvoiceList.recyclerView.setAdapter(invoiceAdapter);

        // Configurar ViewModel
        boolean useMock = getIntent().getBooleanExtra("USE_RETROMOCK", false);
        InvoiceViewModelFactory invoiceViewModelFactory = new InvoiceViewModelFactory(useMock, this);
        invoiceViewModel = new ViewModelProvider(this, invoiceViewModelFactory).get(InvoiceViewModel.class);

        // --- 1. OBSERVER DE CARGA (SHIMMER) ---
        invoiceViewModel.getIsLoading().observe(this, isLoading -> {
            // Siempre que cambie el estado de carga, recalculamos qué se debe ver
            actualizarEstadoUI();
        });

        // --- 2. OBSERVER DE DATOS ---
        invoiceViewModel.getFacturas().observe(this, facturas -> {
            invalidateOptionsMenu();
            if (facturas != null) {
                invoiceAdapter.setFacturas(facturas);
                // Cuando llegan datos nuevos, recalculamos qué se debe ver
                actualizarEstadoUI();
            } else {
                Toast.makeText(InvoiceListActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });


        invoiceViewModel.getShowEmptyError().observe(this, showError -> {
                    actualizarEstadoUI();
        });

        bindingInvoiceList.btnVolver.setOnClickListener(v -> finish());

        // --- AÑADIR ESTO ---
        // Configurar el botón "Reintentar" del Empty State
        bindingInvoiceList.btnRetry.setOnClickListener(v -> {
            invoiceViewModel.cargarFacturas();
        });


        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                restoreMainView();
            }
        });
    }

    // En InvoiceListActivity.java

    private void actualizarEstadoUI() {
        Boolean isLoading = invoiceViewModel.getIsLoading().getValue();
        List<Invoice> facturas = invoiceViewModel.getFacturas().getValue();
        Boolean isError = invoiceViewModel.getShowEmptyError().getValue();

        // Protecciones contra nulos
        if (isError == null) isError = false;
        if (isLoading == null) isLoading = false;

        // 1. CARGA (Shimmer)
        if (isLoading) {
            if (bindingInvoiceList.shimmerViewContainer.getVisibility() != View.VISIBLE) {
                bindingInvoiceList.shimmerViewContainer.setVisibility(View.VISIBLE);
                bindingInvoiceList.shimmerViewContainer.startShimmer();
            }
            bindingInvoiceList.recyclerView.setVisibility(View.GONE);
            bindingInvoiceList.layoutErrorState.setVisibility(View.GONE);
            bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE); // ¡Ocultar Empty también!
            return;
        }

        // 2. Apagar Shimmer
        bindingInvoiceList.shimmerViewContainer.stopShimmer();
        bindingInvoiceList.shimmerViewContainer.setVisibility(View.GONE);

        // 3. ESTADOS FINALES
        if (isError) {
            // CASO A: Error de Red (Wifi off)
            bindingInvoiceList.layoutErrorState.setVisibility(View.VISIBLE); // MOSTRAR ERROR

            bindingInvoiceList.recyclerView.setVisibility(View.GONE);
            bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);    // OCULTAR EMPTY

        } else if (facturas != null && !facturas.isEmpty()) {
            // CASO B: Hay datos (Lista normal)
            bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);     // MOSTRAR LISTA

            bindingInvoiceList.layoutErrorState.setVisibility(View.GONE);    // OCULTAR ERROR
            bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);    // OCULTAR EMPTY

        }else {
            // CASO C: Lista vacía
            // Solo mostramos Empty State si NO hay error Y la lista de facturas NO es nula
            // Esto evita que se muestre durante la inicialización o transiciones
            if (!isError && facturas != null) {
                bindingInvoiceList.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);
            }

            bindingInvoiceList.recyclerView.setVisibility(View.GONE);
            bindingInvoiceList.layoutErrorState.setVisibility(View.GONE);
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        MenuItem filtroItem = menu.findItem(R.id.action_filters);

        Boolean loading = invoiceViewModel.getIsLoading().getValue();
        boolean isNotLoading = loading == null || !loading;

        filtroItem.setEnabled(invoiceViewModel.hayDatosCargados() && isNotLoading);
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
        FilterFragment filterFragment = new FilterFragment();
        bindingInvoiceList.fragmentContainer.setVisibility(View.VISIBLE);
        bindingInvoiceList.toolbar.setVisibility(View.GONE);
        bindingInvoiceList.recyclerView.setVisibility(View.GONE);
        // Ocultamos empty state por si acaso estaba visible
        bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, filterFragment, "FILTRO_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);

        // Al volver, simplemente forzamos una actualización de UI
        // para que decida si muestra Shimmer, Lista o Empty State según el estado actual.
        actualizarEstadoUI();
    }
}
