package com.nexosolar.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nexosolar.android.NexoSolarApplication;
import com.nexosolar.android.R;
import com.nexosolar.android.databinding.ActivityMainBinding;
import com.nexosolar.android.ui.invoices.InvoiceListActivity;
import com.nexosolar.android.ui.smartsolar.SmartSolarActivity;

/**
 * MainActivity
 *
 * Pantalla principal de la aplicación que actúa como dashboard.
 * Permite navegar a las secciones de Facturas y Smart Solar.
 * También ofrece un toggle para cambiar entre API real (Retrofit) y datos simulados (RetroMock).
 */
public class MainActivity extends AppCompatActivity {

    // ===== Variables de instancia =====

    private ActivityMainBinding binding;
    private boolean useMock = true;

    // ===== Ciclo de vida =====

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWindowInsets();
        setupUserGreeting();
        setupCardListeners();
        setupMockToggle();
    }

    // ===== Configuración de UI =====

    /**
     * Configura los insets de la ventana para soporte de pantalla completa (Edge-to-Edge).
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura el saludo personalizado y la dirección del usuario en la UI.
     * Los datos actuales están hardcodeados por ausencia de lógica de autenticación.
     */
    private void setupUserGreeting() {
        String nombreUsuario = "USUARIO";
        String saludoCompleto = getString(R.string.greeting_user, nombreUsuario);
        binding.tvGreeting.setText(saludoCompleto);

        String direccionGuardada = getString(R.string.avenida_de_la_constituci_n_45);
        String miDireccion = getString(R.string.direccion_con_formato, direccionGuardada);
        binding.tvAddress.setText(
                HtmlCompat.fromHtml(miDireccion, HtmlCompat.FROM_HTML_MODE_LEGACY)
        );
    }

    /**
     * Configura los listeners de las tarjetas de navegación (Facturas y Smart Solar).
     */
    private void setupCardListeners() {
        binding.cardFacturas.setOnClickListener(v -> navigateToInvoices());
        binding.cardSmartSolar.setOnClickListener(v -> navigateToSmartSolar());
    }

    /**
     * Configura el toggle que permite alternar entre API real (Retrofit) y mock (RetroMock).
     */
    private void setupMockToggle() {
        binding.btToggleApi.setChecked(useMock);
        binding.btToggleApi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            useMock = isChecked;

            // ACTUALIZAMOS EL GRAFO DE DEPENDENCIAS GLOBAL
            ((NexoSolarApplication) getApplication()).switchDataModule(useMock);

            String mode = useMock ? "Using RetroMock" : "Using RetroFit";
            Toast.makeText(MainActivity.this, mode, Toast.LENGTH_SHORT).show();
        });
    }

    // ===== Navegación =====

    /**
     * Navega a la pantalla de lista de facturas, pasando el modo de API seleccionado.
     */
    private void navigateToInvoices() {
        Intent intent = new Intent(MainActivity.this, InvoiceListActivity.class);
        intent.putExtra("USE_RETROMOCK", useMock);
        startActivity(intent);
    }

    /**
     * Navega a la pantalla de Smart Solar.
     */
    private void navigateToSmartSolar() {
        Intent intent = new Intent(MainActivity.this, SmartSolarActivity.class);
        startActivity(intent);
    }
}
