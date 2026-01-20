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

/**
 * InvoiceListActivity
 *
 * Pantalla principal de gestión de facturas.
 * Muestra un RecyclerView con la lista de facturas, shimmer de carga,
 * pantallas de error diferenciadas (red/servidor) y estados vacíos.
 * Permite aplicar filtros mediante un fragmento modal (FilterFragment).
 */
public class InvoiceListActivity extends AppCompatActivity {

    // ===== Variables de instancia =====

    private ActivityInvoiceListBinding binding;
    private InvoiceAdapter invoiceAdapter;
    private InvoiceViewModel invoiceViewModel;

    // ===== Ciclo de vida =====

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInvoiceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        setupRecyclerView();
        setupViewModel();
        setupObservers();
        setupListeners();
        setupBackStackListener();
    }

    // ===== Configuración inicial =====

    /**
     * Configura el RecyclerView con su adaptador y layout manager.
     */
    private void setupRecyclerView() {
        invoiceAdapter = new InvoiceAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(invoiceAdapter);
    }

    /**
     * Inicializa el ViewModel con la factory apropiada según el modo (Mock/Real API).
     */
    private void setupViewModel() {
        boolean useMock = getIntent().getBooleanExtra("USE_RETROMOCK", false);
        InvoiceViewModelFactory factory = new InvoiceViewModelFactory(useMock, this);
        invoiceViewModel = new ViewModelProvider(this, factory).get(InvoiceViewModel.class);
    }

    /**
     * Configura los observadores de LiveData del ViewModel.
     * Observa estados de carga, datos, errores y estado vacío.
     */
    private void setupObservers() {
        invoiceViewModel.getIsLoading().observe(this, isLoading -> actualizarEstadoUI());

        invoiceViewModel.getFacturas().observe(this, facturas -> {
            invalidateOptionsMenu();
            if (facturas != null) {
                invoiceAdapter.setFacturas(facturas);
                actualizarEstadoUI();
            } else {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });

        invoiceViewModel.getErrorTypeState().observe(this, errorType -> actualizarEstadoUI());
        invoiceViewModel.getShowEmptyError().observe(this, showError -> actualizarEstadoUI());
    }

    /**
     * Configura listeners de botones de acción (Volver, Reintentar).
     */
    private void setupListeners() {
        binding.btnVolver.setOnClickListener(v -> finish());
        binding.btnRetry.setOnClickListener(v -> invoiceViewModel.cargarFacturas());
    }

    /**
     * Escucha cambios en el BackStack para restaurar la vista principal al cerrar fragmentos.
     */
    private void setupBackStackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                restoreMainView();
            }
        });
    }

    // ===== Gestión de estados de UI =====

    /**
     * Actualiza la UI según el estado actual (carga, error, datos, vacío).
     * Prioridad: Loading > Error > Datos > Empty State
     */
    private void actualizarEstadoUI() {
        Boolean isLoading = invoiceViewModel.getIsLoading().getValue();
        List<Invoice> facturas = invoiceViewModel.getFacturas().getValue();
        InvoiceViewModel.ErrorType errorType = invoiceViewModel.getErrorTypeState().getValue();

        if (isLoading == null) isLoading = false;
        if (errorType == null) errorType = InvoiceViewModel.ErrorType.NONE;

        // Estado de carga (shimmer)
        if (isLoading) {
            mostrarShimmer();
            return;
        }

        ocultarShimmer();

        // Estado de error (red o servidor)
        if (errorType != InvoiceViewModel.ErrorType.NONE) {
            mostrarError(errorType);
            return;
        }

        // Estado con datos o vacío
        if (facturas != null && !facturas.isEmpty()) {
            mostrarLista();
        } else {
            mostrarEmptyState();
        }
    }

    private void mostrarShimmer() {
        if (binding.shimmerViewContainer.getVisibility() != View.VISIBLE) {
            binding.shimmerViewContainer.setVisibility(View.VISIBLE);
            binding.shimmerViewContainer.startShimmer();
        }
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void ocultarShimmer() {
        binding.shimmerViewContainer.stopShimmer();
        binding.shimmerViewContainer.setVisibility(View.GONE);
    }

    private void mostrarError(InvoiceViewModel.ErrorType tipo) {
        configurarVistaError(tipo);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void mostrarLista() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void mostrarEmptyState() {
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    /**
     * Configura la vista de error según el tipo (red o servidor).
     * Ajusta icono, título y descripción dinámicamente.
     */
    private void configurarVistaError(InvoiceViewModel.ErrorType tipo) {
        if (tipo == InvoiceViewModel.ErrorType.NETWORK) {
            binding.ivError.setImageResource(R.drawable.ic_wifi_off_24);
            binding.tvError.setText(R.string.error_conexion);
            binding.tvErrorDescription.setText(R.string.error_conexion_description_message);
        } else if (tipo == InvoiceViewModel.ErrorType.SERVER_GENERIC) {
            binding.ivError.setImageResource(R.drawable.ic_server_off_24);
            binding.tvError.setText(R.string.error_conexion_servidor);
            binding.tvErrorDescription.setText(R.string.error_conexion_servidor_description_message);
        }
    }

    // ===== Menú y filtros =====

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

    /**
     * Muestra el fragmento de filtros en modo modal.
     * Oculta la toolbar y el RecyclerView durante la edición de filtros.
     */
    private void mostrarFiltroFragment() {
        FilterFragment filterFragment = new FilterFragment();

        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.toolbar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, filterFragment, "FILTRO_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Restaura la vista principal al cerrar el fragmento de filtros.
     * Recalcula el estado de UI para reflejar cambios en los datos.
     */
    public void restoreMainView() {
        binding.toolbar.setVisibility(View.VISIBLE);
        binding.fragmentContainer.setVisibility(View.GONE);
        actualizarEstadoUI();
    }
}
