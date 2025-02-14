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

        // Crea insets para poder visualizar mejor la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(bindingInvoiceList.InvoiceList, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            bindingInvoiceList.InvoiceList.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar RecyclerView
        adapter = new InvoiceAdapter();
        bindingInvoiceList.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindingInvoiceList.recyclerView.setAdapter(adapter);

        // Configurar ViewModel

        // Obtiene el valor booleano de la intención que inició esta actividad
        // Se verifica si se debe usar Retromock (simulación de datos) o no

        boolean useMock = getIntent().getBooleanExtra("USE_RETROMOCK", false);


        // Crea una instancia del ViewModel usando ViewModelProvider
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {

            @Override
            //Crea y devuelve una instancia de ViewModel
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

                // Crea un nuevo InvoiceViewModel, pasando el valor de useMock
                // y el contexto de la actividad
                return (T) new InvoiceViewModel(useMock, InvoiceListActivity.this);
            }

        }).get(InvoiceViewModel.class);

        // Establece un observador en la lista de facturas del ViewModel
        viewModel.getFacturas().observe(this, facturas -> {

            if (facturas != null) {
                adapter.setFacturas(facturas);
            }
        });

        // Cargar datos desde la API o Retromock
        viewModel.cargarFacturas();

        // Botón para volver a la actividad principal
        bindingInvoiceList.btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(InvoiceListActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Cierra esta actividad
        });

    }
}
