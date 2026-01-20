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

        // 3. OBSERVER DE TIPO DE ERROR
        invoiceViewModel.getErrorTypeState().observe(this, errorType -> {
            actualizarEstadoUI();
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
        InvoiceViewModel.ErrorType errorType = invoiceViewModel.getErrorTypeState().getValue();

        // Null safety
        if (isLoading == null) isLoading = false;
        if (errorType == null) errorType = InvoiceViewModel.ErrorType.NONE;

        // --- 1. ESTADO DE CARGA (PRIORIDAD ALTA) ---
        if (isLoading) {
            if (bindingInvoiceList.shimmerViewContainer.getVisibility() != View.VISIBLE) {
                bindingInvoiceList.shimmerViewContainer.setVisibility(View.VISIBLE);
                bindingInvoiceList.shimmerViewContainer.startShimmer();
            }
            bindingInvoiceList.recyclerView.setVisibility(View.GONE);
            bindingInvoiceList.layoutErrorState.setVisibility(View.GONE);
            bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);
            return;
        }

        // Apagar shimmer si no carga
        bindingInvoiceList.shimmerViewContainer.stopShimmer();
        bindingInvoiceList.shimmerViewContainer.setVisibility(View.GONE);

        // --- 2. ESTADO DE ERROR (PRIORIDAD MEDIA) ---
        if (errorType != InvoiceViewModel.ErrorType.NONE) {
            // Configurar UI según el tipo de error
            configurarVistaError(errorType);

            bindingInvoiceList.layoutErrorState.setVisibility(View.VISIBLE); // Mostrar Error

            bindingInvoiceList.recyclerView.setVisibility(View.GONE);
            bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);
            return;
        }

        // --- 3. ESTADO DE DATOS O VACÍO (PRIORIDAD BAJA) ---
        if (facturas != null && !facturas.isEmpty()) {
            // Hay facturas -> Mostrar Lista
            bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);

            bindingInvoiceList.layoutErrorState.setVisibility(View.GONE);
            bindingInvoiceList.layoutEmptyState.setVisibility(View.GONE);
        } else {
            // No hay error y lista vacía -> Mostrar Empty State (Lupa)
            // Solo si no estamos en un limbo nulo (defensivo)
            if (facturas != null) {
                bindingInvoiceList.layoutEmptyState.setVisibility(View.VISIBLE);
            }

            bindingInvoiceList.recyclerView.setVisibility(View.GONE);
            bindingInvoiceList.layoutErrorState.setVisibility(View.GONE);
        }
    }

    /**
     * Configura textos e imagen del layout de error dinámicamente al ser prácticamente iguales
     */
    private void configurarVistaError(InvoiceViewModel.ErrorType tipo) {
        if (tipo == InvoiceViewModel.ErrorType.NETWORK) {
            // CASO: Sin Internet (Wifi off) o error de conexión genérico
            bindingInvoiceList.ivError.setImageResource(R.drawable.ic_wifi_off_24);
            bindingInvoiceList.tvError.setText(R.string.error_conexion);
            bindingInvoiceList.tvErrorDescription.setText(R.string.error_conexion_description_message);


        } else if (tipo == InvoiceViewModel.ErrorType.SERVER_GENERIC) {
            // CASO: Error Servidor (Nube rota / Warning)
            bindingInvoiceList.ivError.setImageResource(R.drawable.ic_server_off_24);

            bindingInvoiceList.tvError.setText(R.string.error_conexion_servidor);
            bindingInvoiceList.tvErrorDescription.setText(R.string.error_conexion_servidor_description_message);
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
