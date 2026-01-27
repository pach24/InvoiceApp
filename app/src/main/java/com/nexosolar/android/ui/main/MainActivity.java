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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean useMock = true;
    private boolean useAltUrl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupWindowInsets();
        setupUserGreeting();
        setupCardListeners();
        setupApiControls();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

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

    private void setupCardListeners() {
        binding.cardFacturas.setOnClickListener(v -> navigateToInvoices());
        binding.cardSmartSolar.setOnClickListener(v -> navigateToSmartSolar());
    }

    /**
     * Configura los switches de API con lógica de habilitación/deshabilitación
     */
    private void setupApiControls() {
        // Estado inicial
        binding.btToggleApi.setChecked(useMock);
        binding.switchAltUrl.setChecked(useAltUrl);

        updateUrlSwitchState();

        // Listener del switch principal: Mock vs Real
        binding.btToggleApi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            useMock = isChecked;

            // Si activamos Mock, desactivar URL alt
            if (useMock) {
                useAltUrl = false;
                binding.switchAltUrl.setOnCheckedChangeListener(null);
                binding.switchAltUrl.setChecked(false);
                binding.switchAltUrl.setOnCheckedChangeListener(
                        (btn, checked) -> {
                            useAltUrl = checked;
                            updateDataModule();
                        }
                );
            }

            updateUrlSwitchState();
            updateDataModule();
        });

        // Listener del switch secundario: URL 1 vs URL 2
        binding.switchAltUrl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            useAltUrl = isChecked;
            updateDataModule();
        });

        updateDataModule();
    }

    /**
     * Actualiza el estado visual y de habilitación del switch de URL
     * - enabled=true y alpha=1.0 cuando useMock=false
     * - enabled=false y alpha=0.5 cuando useMock=true
     */
    private void updateUrlSwitchState() {
        boolean shouldEnable = !useMock;
        binding.switchAltUrl.setEnabled(shouldEnable);
        binding.switchAltUrl.setAlpha(shouldEnable ? 1.0f : 0.5f);
    }

    private void updateDataModule() {
        ((NexoSolarApplication) getApplication()).switchDataModule(useMock, useAltUrl);

        String modeMsg = useMock ? "Modo: Mock (RetroMock)" : "Modo: Real (Retrofit)";
        String urlMsg = (!useMock && useAltUrl) ? " - URL2 (No funciona)" : (!useMock ? " - URL1 (Funciona)" : "");
        Toast.makeText(this, modeMsg + urlMsg, Toast.LENGTH_SHORT).show();
    }

    private void navigateToInvoices() {
        Intent intent = new Intent(MainActivity.this, InvoiceListActivity.class);
        intent.putExtra("USE_RETROMOCK", useMock);
        startActivity(intent);
    }

    private void navigateToSmartSolar() {
        Intent intent = new Intent(MainActivity.this, SmartSolarActivity.class);
        startActivity(intent);
    }
}
