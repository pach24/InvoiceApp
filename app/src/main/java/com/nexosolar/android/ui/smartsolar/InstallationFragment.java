package com.nexosolar.android.ui.smartsolar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexosolar.android.databinding.FragmentInstallationBinding;

public class InstallationFragment extends Fragment {

    private FragmentInstallationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInstallationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // Ejemplo de cómo actualizarías los datos en el futuro
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // binding.tvSolarValue.setText("450 w");
        // binding.tvAutoconsumoValue.setText("100%");
    }
}
