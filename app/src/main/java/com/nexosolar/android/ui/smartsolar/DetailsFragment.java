package com.nexosolar.android.ui.smartsolar;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.databinding.FragmentDetailsBinding;



public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private InstallationViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Configurar ViewModel (Inyección manual simple)

        boolean useMock = true;
        InstallationViewModelFactory factory = new InstallationViewModelFactory(requireContext(), useMock);
        viewModel = new ViewModelProvider(this, factory).get(InstallationViewModel.class);

        // 2. Observar estado de carga (Shimmer)
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.shimmerViewContainer.startShimmer();
                binding.shimmerViewContainer.setVisibility(View.VISIBLE);
                binding.contentLayout.setVisibility(View.GONE);
            } else {
                binding.shimmerViewContainer.stopShimmer();
                binding.shimmerViewContainer.setVisibility(View.GONE);
                binding.contentLayout.setVisibility(View.VISIBLE);
            }
        });

        // 3. Observar datos exitosos
        viewModel.getInstallation().observe(getViewLifecycleOwner(), installation -> {
            if (installation != null) {
                binding.tvCau.setText(installation.getCau());
                binding.tvStatus.setText(installation.getStatus());
                binding.tvType.setText(installation.getType());
                binding.tvCompensation.setText(installation.getCompensation());
                binding.tvPower.setText(installation.getPower());
            }
        });

        // 4. Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            binding.tvStatus.setText(error); // Opcional
        });

        // 5. Pedir datos (si es la primera vez)
        // Usamos una comprobación simple para no recargar al rotar si ya tenemos datos
        if (viewModel.getInstallation().getValue() == null) {
            viewModel.loadInstallationDetails();
        }

        // Listener del botón info
        binding.ivInfo.setOnClickListener(v -> showInfoDialog());
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Estado solicitud autoconsumo")
                .setMessage("El tiempo estimado de activación de tu autoconsumo es de 1 a 2 meses, éste variará en función de tu comunidad autónoma y distribuidora")
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
