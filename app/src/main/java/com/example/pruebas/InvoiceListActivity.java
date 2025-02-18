package com.example.pruebas;

import android.os.Bundle;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pruebas.databinding.ActivityInvoiceListBinding;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
    }
}
