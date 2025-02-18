package com.example.pruebas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
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

        // Configurar ViewModel usando ViewModelProvider.Factory
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new InvoiceViewModel(useMock, InvoiceListActivity.this);
            }
        }).get(InvoiceViewModel.class);

        // Observar los datos de facturas y actualizar la UI
        viewModel.getFacturas().observe(this, facturas -> {
            if (facturas != null) {
                adapter.setFacturas(facturas);
            }
        });

        // Cargar datos desde el ViewModel
        viewModel.cargarFacturas();

        // Botón para volver a la actividad principal
        bindingInvoiceList.btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(InvoiceListActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cierra esta actividad para evitar volver con el botón atrás
        });

        // Configuración de la Toolbar
        setSupportActionBar(bindingInvoiceList.toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflar el menú desde el archivo XML
        getMenuInflater().inflate(R.menu.fragment_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_filters) {
            // Actualmente no hace nada, pero aquí manejarías la acción del menú
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
