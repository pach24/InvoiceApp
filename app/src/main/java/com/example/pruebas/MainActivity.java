package com.example.pruebas;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.widget.*;



import com.example.pruebas.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    // Para calcular el cuadrado
    private EditText etAlcuadrado;
    private Button btAlcuadrado;
    private TextView tvAlCuadrado;
    private AlCuadradoViewModel viewModel;
    private InvoiceViewModel invoiceViewModel; // Declarar el InvoiceViewModel


    private boolean useMock = false; // Variable para alternar entre Retrofit y Retromock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //Asignar el binding a la actividad
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);



        // Crea insets para poder visualizar mejor la pantalla
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.main.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        configView();

        Button btIrListaFacturas = findViewById(R.id.btIrListaFacturas);
        Button toggleRetromock = findViewById(R.id.toggleButton);

        toggleRetromock.setOnClickListener(v -> {
            // Alternar el estado de useMock
            useMock = !useMock;

            // Mostrar el Toast con el estado actual de useMock
            String mensaje = "Estado de RetroMock: " + useMock;
            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
        });
        // Configurar un listener para el botón
        btIrListaFacturas.setOnClickListener(v -> {
            // Crear un intent para iniciar la nueva actividad
            Intent intent = new Intent(MainActivity.this, InvoiceListActivity.class);
            intent.putExtra("USE_RETROMOCK", useMock); // Pasa el valor de useMock
            startActivity(intent); // Iniciar la nueva actividad
        });

        invoiceViewModel = new InvoiceViewModel(useMock, MainActivity.this);

    }

    private void configView(){

        viewModel = new ViewModelProvider(this).get(AlCuadradoViewModel.class);

        tvAlCuadrado = binding.tvAlCuadrado;
        etAlcuadrado = binding.edAlCuadrado;
        btAlcuadrado= binding.btCalcular;




        btAlcuadrado.setOnClickListener(v -> viewModel.alCuadrado(etAlcuadrado.getText().toString()));

        final Observer <String> observer = resultado -> tvAlCuadrado.setText(resultado);

        viewModel.getResultado().observe(this, observer);


    }





}