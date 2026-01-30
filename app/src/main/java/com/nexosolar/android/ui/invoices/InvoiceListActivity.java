package com.nexosolar.android.ui.invoices;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nexosolar.android.NexoSolarApplication;
import com.nexosolar.android.R;
import com.nexosolar.android.data.DataModule;
import com.nexosolar.android.databinding.ActivityInvoiceListBinding;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;
import com.nexosolar.android.ui.invoices.managers.InvoiceStateManager.ViewState;
import java.util.List;
import androidx.activity.OnBackPressedCallback;


public class InvoiceListActivity extends AppCompatActivity {

    private ActivityInvoiceListBinding binding;
    private InvoiceViewModel invoiceViewModel;
    private InvoiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInvoiceListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        setupViewModel();
        setupRecyclerView();
        setupObservers();
        setupListeners();
        setupBackPressHandler();
    }


    private void setupViewModel() {
        // 1. Obtener el DataModule desde la Application
        NexoSolarApplication app = (NexoSolarApplication) getApplication();
        DataModule dataModule = app.getDataModule();

        // 2. Crear el Repositorio y los Use Cases
        // (Asegúrate de tener acceso a los constructores de los UseCases)
        InvoiceRepository repository = dataModule.provideInvoiceRepository();
        GetInvoicesUseCase getUseCase = new GetInvoicesUseCase(repository);
        FilterInvoicesUseCase filterUseCase = new FilterInvoicesUseCase();

        // 3. Usar tu Factory (file:24) para instanciar el ViewModel
        InvoiceViewModelFactory factory = new InvoiceViewModelFactory(getUseCase, filterUseCase);

        // 4. IMPORTANTE: Pasar la factory al ViewModelProvider
        invoiceViewModel = new ViewModelProvider(this, factory).get(InvoiceViewModel.class);
    }



    private void setupRecyclerView() {
        adapter = new InvoiceAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        // Observar la lista de facturas
        invoiceViewModel.getFacturas().observe(this, facturas -> {
            adapter.setFacturas(facturas);
            actualizarEstadoUI();
        });

        // Observar el estado de la vista (LOADING, ERROR, DATA, EMPTY)
        invoiceViewModel.getViewState().observe(this, state -> {
            actualizarEstadoUI();
        });

        // Observar si se debe mostrar el error a pantalla completa
        invoiceViewModel.getShowEmptyError().observe(this, show -> {
            actualizarEstadoUI();
        });
    }

    private void setupListeners() {
        binding.btnRetry.setOnClickListener(v -> invoiceViewModel.cargarFacturas());
        binding.btnVolver.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Actualiza la visibilidad de los componentes de la UI basándose en el estado
     * y los datos proporcionados por el ViewModel.
     * Sigue una jerarquía de prioridades: LOADING > ERROR > DATA/EMPTY.
     */
    private void actualizarEstadoUI() {
        ViewState state = invoiceViewModel.getViewState().getValue();
        List<Invoice> facturas = invoiceViewModel.getFacturas().getValue();
        boolean showEmptyError = Boolean.TRUE.equals(invoiceViewModel.getShowEmptyError().getValue());

        Log.d("UI_STATE", "=== ACTUALIZANDO UI ===");
        Log.d("UI_STATE", "Estado: " + state);
        Log.d("UI_STATE", "Facturas: " + (facturas != null ? facturas.size() : 0));
        Log.d("UI_STATE", "ShowEmptyError: " + showEmptyError);

        if (state == null) {
            Log.d("UI_STATE", "Estado es null, saliendo");
            return;
        }

        // PRIORIDAD 1: Estado de carga
        if (state == ViewState.LOADING) {
            Log.d("UI_STATE", "Mostrando SHIMMER (estado LOADING)");
            mostrarShimmer();
            return;
        }

        Log.d("UI_STATE", "Ocultando shimmer (estado no LOADING)");
        ocultarShimmer();


        // PRIORIDAD 2: Errores críticos (Estado de error sin datos previos)
        // Solo mostramos la pantalla de error si el manager indica que es un error "vacío"
        if (showEmptyError && (state == ViewState.ERROR_NETWORK || state == ViewState.ERROR_SERVER)) {
            mostrarError(state);
            return;
        }

        // PRIORIDAD 3: Resultados (Datos vs Lista Vacía)
        // Solo evaluamos el contenido de la lista si el proceso de carga/filtrado ya terminó.
        if (facturas != null && !facturas.isEmpty()) {
            mostrarLista();
        } else {
            // Solo mostramos el estado vacío si el manager ha confirmado que no hay resultados (ViewState.EMPTY)
            // o si la lista está realmente vacía y no estamos en un estado de error
            mostrarEmptyState();
        }

        // Notificamos al sistema para que actualice la disponibilidad de los botones del menú (filtros)
        invalidateOptionsMenu();
    }


    private void mostrarShimmer() {
        Log.d("UI_STATE", "MOSTRANDO SHIMMER");

        // Asegurarnos de que todos los demás estados estén ocultos
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.GONE);

        // Ocultar la lista actual
        binding.recyclerView.setVisibility(View.GONE);

        // Mostrar y empezar el shimmer
        binding.shimmerViewContainer.setVisibility(View.VISIBLE);
        binding.shimmerViewContainer.startShimmer();

        // Forzar redibujado
        binding.shimmerViewContainer.post(() -> {
            binding.shimmerViewContainer.requestLayout();
        });
    }

    private void ocultarShimmer() {
        binding.shimmerViewContainer.stopShimmer();
        binding.shimmerViewContainer.setVisibility(View.GONE);
    }

    private void mostrarError(ViewState tipo) {
        configurarVistaError(tipo);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void configurarVistaError(ViewState tipo) {
        if (tipo == ViewState.ERROR_NETWORK) {
            binding.ivError.setImageResource(R.drawable.ic_wifi_off_24);
            binding.tvError.setText(R.string.error_conexion);
            binding.tvErrorDescription.setText(R.string.error_conexion_description_message);
        } else {
            binding.ivError.setImageResource(R.drawable.ic_server_off_24);
            binding.tvError.setText(R.string.error_conexion_servidor);
            binding.tvErrorDescription.setText(R.string.error_conexion_servidor_description_message);
        }
    }

    /**
     * Actualiza la UI inmediatamente de forma síncrona.
     * Útil para evitar que se vea el estado anterior al cerrar fragmentos.
     */
    public void actualizarEstadoUIInmediatamente() {
        // Ejecutar en el hilo principal de forma síncrona
        runOnUiThread(() -> {
            ViewState state = invoiceViewModel.getViewState().getValue();
            List<Invoice> facturas = invoiceViewModel.getFacturas().getValue();

            // Actualización mínima y rápida
            if (facturas != null && !facturas.isEmpty()) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                binding.layoutErrorState.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);
                binding.layoutErrorState.setVisibility(View.GONE);
            }

            // Forzar un redibujado
            binding.recyclerView.post(() -> {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        MenuItem filtroItem = menu.findItem(R.id.action_filters);

        ViewState state = invoiceViewModel.getViewState().getValue();
        boolean hasData = invoiceViewModel.hayDatosCargados();
        boolean isNotLoading = state != ViewState.LOADING;

        filtroItem.setEnabled(hasData && isNotLoading);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filters) {
            if (invoiceViewModel.hayDatosCargados()) {
                mostrarFiltroFragment();
            } else {
                Toast.makeText(this, R.string.no_data_to_filter, Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarFiltroFragment() {
        FilterFragment filterFragment = new FilterFragment();
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.toolbar.setVisibility(View.GONE);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, filterFragment, "FILTRO_FRAGMENT");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void restoreMainView() {
        binding.toolbar.setVisibility(View.VISIBLE);
        binding.fragmentContainer.setVisibility(View.GONE);
        actualizarEstadoUI();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FilterFragment filterFragment = (FilterFragment) getSupportFragmentManager()
                        .findFragmentByTag("FILTRO_FRAGMENT");

                if (filterFragment != null && filterFragment.isVisible()) {
                    restoreMainView();
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Si no hay fragment visible, hacer el comportamiento normal de back
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }
}
