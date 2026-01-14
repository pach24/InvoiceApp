package com.nexosolar.android.ui.smartsolar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nexosolar.android.databinding.FragmentDetailsBinding;

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. CARGAR DATOS MOCK (Simulando una llamada a API)
        loadMockData();

        // 2. CONFIGURAR EL CLICK DE LA 'i'
        binding.ivInfo.setOnClickListener(v -> showInfoDialog());
    }

    private void loadMockData() {
        // En un caso real
        binding.tvCau.setText("ES0021000000001994LJ1FA000");
        binding.tvStatus.setText("No hemos recibido ninguna solicitud de autoconsumo");
        binding.tvType.setText("Con excedentes y compensación Individual - Consumo");
        binding.tvCompensation.setText("Precio PVPC");
        binding.tvPower.setText("5kWp");
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Estado solicitud autoconsumo")
                .setMessage("El tiempo estimado de activación de tu autoconsumo es de 1 a 2 meses, éste variará en función de tu comunidad autónoma y distribuidora")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
        // Nota: Si quieres el diseño EXACTO del popup verde y redondeado,
        // necesitaríamos crear un layout XML personalizado para el diálogo.
        // ¿Quieres que hagamos eso o te vale con el estándar de Android?
    }
}
