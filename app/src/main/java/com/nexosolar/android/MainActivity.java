package com.nexosolar.android;

import android.os.Bundle;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Toast;
import com.nexosolar.android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private boolean useMock = true; // Variable para alternar entre Retrofit y Retromock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilitar EdgeToEdge para pantallas completas

        // Inflar el layout usando ViewBinding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar insets para manejar la barra de sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar el botón para ir a la lista de facturas
        binding.btFacturas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InvoiceListActivity.class);
            intent.putExtra("USE_RETROMOCK", useMock); // Pasar el estado de useMock
            startActivity(intent); // Iniciar la actividad
        });

        // Configurar el botón para alternar el estado de useMock
        binding.btToggleApi.setOnClickListener(v -> {
            useMock = !useMock;
            String message = "RetroMock state: " + useMock;
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }
}
