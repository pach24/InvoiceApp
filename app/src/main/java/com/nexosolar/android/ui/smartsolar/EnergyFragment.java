package com.nexosolar.android.ui.smartsolar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexosolar.android.databinding.FragmentEnergyBinding;

/**
 * Fragment que muestra métricas y estadísticas de consumo energético.
 *
 * Responsabilidades:
 * - Renderizar gráficos de consumo/producción energética
 * - Futuro: integrar ViewModel para obtener datos históricos
 */
public class EnergyFragment extends Fragment {

    // ===== Variables de instancia =====

    private FragmentEnergyBinding binding;

    // ===== Métodos del ciclo de vida =====

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEnergyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Posible mejora: Implementar observación de LiveData con métricas energéticas
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
