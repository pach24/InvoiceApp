package com.nexosolar.android.ui.smartsolar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexosolar.android.databinding.FragmentInstallationBinding;

/**
 * Fragment que muestra información general de la instalación solar.
 *
 * Responsabilidades:
 * - Renderizar la vista principal con gráficos y métricas de la instalación
 * - Futuro: integrar ViewModel para cargar datos dinámicos
 */
public class InstallationFragment extends Fragment {

    // ===== Variables de instancia =====

    private FragmentInstallationBinding binding;

    // ===== Métodos del ciclo de vida =====

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInstallationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Posinle mejora Conectar ViewModel para cargar datos de producción solar y autoconsumo
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
