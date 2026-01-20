package com.nexosolar.android.ui.main;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nexosolar.android.R;
import com.nexosolar.android.databinding.ActivityMainBinding;
import com.nexosolar.android.ui.invoices.InvoiceListActivity;
import com.nexosolar.android.ui.smartsolar.SmartSolarActivity;

public class MainActivity extends AppCompatActivity {

    // Variable para alternar entre Retrofit y Retromock
    // Por defecto true (Mock) para desarrollo
    private boolean useMock = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inflar el layout usando ViewBinding
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String nombreUsuario = "USUARIO"; //Hardcodeado porque no tengo lógica para un login y poder extraer los datos
        String saludoCompleto = getString(R.string.greeting_user, nombreUsuario);
        binding.tvGreeting.setText(saludoCompleto);

        String direccionGuardada = getString(R.string.avenida_de_la_constituci_n_45);

        String miDireccion = getString(R.string.direccion_con_formato, direccionGuardada);


        binding.tvAddress.setText(HtmlCompat.fromHtml(
                miDireccion,
                HtmlCompat.FROM_HTML_MODE_LEGACY
        ));


        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Aplicamos padding solo abajo para no cortar el header verde en la status bar
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar el listener en la TARJETA DE FACTURAS
        binding.cardFacturas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InvoiceListActivity.class);
            intent.putExtra("USE_RETROMOCK", useMock);
            startActivity(intent);
        });

        // Configurar el listener en la TARJETA DE SMART SOLAR
        binding.cardSmartSolar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SmartSolarActivity.class);
            startActivity(intent);
        });


        binding.btToggleApi.setChecked(useMock);


        binding.btToggleApi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            useMock = isChecked;
            String mode = useMock ? "Using RetroMock" : "Using RetroFit";
            Toast.makeText(MainActivity.this, mode, Toast.LENGTH_SHORT).show();
        });
    }
}
