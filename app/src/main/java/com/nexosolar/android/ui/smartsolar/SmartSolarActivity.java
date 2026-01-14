package com.nexosolar.android.ui.smartsolar;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import com.nexosolar.android.databinding.ActivitySmartSolarBinding;

public class SmartSolarActivity extends AppCompatActivity {

    private ActivitySmartSolarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inflar layout
        binding = ActivitySmartSolarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Configurar el botón de volver atrás
        binding.backButton.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        // ---------------------------------------------------------------
        // 3. CONFIGURACIÓN DE PESTAÑAS
        // ---------------------------------------------------------------

        // A. Asignar el adaptador al ViewPager2
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // B. Conectar TabLayout con ViewPager2 usando TabLayoutMediator
        // Esto crea las pestañas automáticamente y sincroniza el scroll
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

    // 4. CLASE INTERNA: ADAPTADOR
    // Esta clase gestiona qué Fragment se muestra en cada posición
    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Devuelve el Fragment correspondiente a cada pestaña
            switch (position) {
                case 0:
                    return new InstallationFragment(); // Pestaña 1
                case 1:
                    return new EnergyFragment(); //Pestaña 2
                case 2:
                    return  new DetailsFragment();
                default:
                    return new InstallationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Número total de pestañas
        }
    }
}
