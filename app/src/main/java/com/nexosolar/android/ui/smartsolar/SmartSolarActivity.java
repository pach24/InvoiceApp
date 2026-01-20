package com.nexosolar.android.ui.smartsolar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nexosolar.android.databinding.ActivitySmartSolarBinding;

/**
 * Pantalla principal del módulo SmartSolar.
 * Gestiona la navegación entre las pestañas de información de la instalación
 * mediante ViewPager2 y TabLayout.
 */
public class SmartSolarActivity extends AppCompatActivity {

    // ===== Variables de instancia =====

    private ActivitySmartSolarBinding binding;

    // ===== Métodos del ciclo de vida =====

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySmartSolarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBackButton();
        setupTabs();
    }

    // ===== Métodos privados =====

    /**
     * Configura el comportamiento del botón de navegación hacia atrás.
     */
    private void setupBackButton() {
        binding.backButton.setOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );
    }

    /**
     * Inicializa el ViewPager2 y sincroniza las pestañas con TabLayoutMediator.
     * Se definen tres pestañas: Mi instalación, Energía y Detalles.
     */
    private void setupTabs() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Mi instalación");
                    break;
                case 1:
                    tab.setText("Energía");
                    break;
                case 2:
                    tab.setText("Detalles");
                    break;
            }
        }).attach();
    }

    // ===== Clases internas =====

    /**
     * Adaptador que gestiona la creación de fragments para cada pestaña del ViewPager2.
     */
    private static class ViewPagerAdapter extends FragmentStateAdapter {

        private static final int TAB_COUNT = 3;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new InstallationFragment();
                case 1:
                    return new EnergyFragment();
                case 2:
                    return new DetailsFragment();
                default:
                    return new InstallationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return TAB_COUNT;
        }
    }
}
