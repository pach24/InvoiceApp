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

/**
 * Activity principal para mostrar la lista de facturas.
 * Ahora solo se encarga de la navegación y la coordinación de vistas.
 */
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

        // Observer principal de la lista de facturas
        invoiceViewModel.getFacturas().observe(this, facturas -> {
            bindingInvoiceList.shimmerViewContainer.stopShimmer();
            bindingInvoiceList.shimmerViewContainer.setVisibility(View.GONE);
            invalidateOptionsMenu();

            if (facturas != null) {
                invoiceAdapter.setFacturas(facturas);

                // Mostrar/ocultar el "Empty State"
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

        // LISTENER PARA DETECTAR CUANDO SE CIERRA EL FRAGMENTO
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // Si la pila está vacía (0), significa que hemos vuelto a la Activity base
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                restoreMainView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter, menu);

        // Deshabilitar el ítem de filtro si no hay datos cargados
        MenuItem filtroItem = menu.findItem(R.id.action_filters);
        filtroItem.setEnabled(invoiceViewModel.hayDatosCargados());

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
     * Muestra el fragmento de filtros
     */
    private void mostrarFiltroFragment() {
        FilterFragment filterFragment = new FilterFragment();
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

    /**
     * Restaura la vista principal ocultando el fragmento de filtros
     */
    public void restoreMainView() {
        bindingInvoiceList.toolbar.setVisibility(View.VISIBLE);
        bindingInvoiceList.recyclerView.setVisibility(View.VISIBLE);
        bindingInvoiceList.fragmentContainer.setVisibility(View.GONE);
    }
}
