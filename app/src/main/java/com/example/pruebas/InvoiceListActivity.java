package com.example.pruebas;

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

import com.example.pruebas.databinding.ActivityInvoiceListBinding;

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
        getMenuInflater().inflate(R.menu.fragment_filter, menu);
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
        bindingInvoiceList.fragmentContainer.setVisibility(View.VISIBLE);
        FilterFragment filterFragment = new FilterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, filterFragment);
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
}
