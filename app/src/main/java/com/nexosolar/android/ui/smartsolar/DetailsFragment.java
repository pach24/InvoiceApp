package com.nexosolar.android.ui.smartsolar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nexosolar.android.data.remote.ApiClientManager;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.repository.InstallationRepositoryImpl;
import com.nexosolar.android.domain.models.Installation;
import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;
import com.nexosolar.android.databinding.FragmentDetailsBinding;

public class DetailsFragment extends Fragment {

    private FragmentDetailsBinding binding;
    private GetInstallationDetailsUseCase getInstallationDetailsUseCase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Iniciar la animación del shimmer mientras se cargan los datos
        binding.shimmerViewContainer.startShimmer();

        // Configurar dependencias (Inyección manual)
        setupDependencies();

        // Cargar datos
        loadData();

        // Configurar UI
        binding.ivInfo.setOnClickListener(v -> showInfoDialog());
    }

    private void setupDependencies() {
        // 1. Obtener ApiService
        ApiService apiService = ApiClientManager.getInstance()
                .getApiService(true, requireContext());

        // 2. Crear repositorio
        InstallationRepository repository = new InstallationRepositoryImpl(apiService);

        // 3. Crear caso de uso
        getInstallationDetailsUseCase = new GetInstallationDetailsUseCase(repository);
    }

    private void loadData() {
        // Ejecutar caso de uso con callback
        getInstallationDetailsUseCase.execute(new InstallationRepository.InstallationCallback() {
            @Override
            public void onSuccess(Installation installation) {
                // Asegurarnos de tocar la UI en el hilo principal
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    // 1. Rellenar datos
                    binding.tvCau.setText(installation.getCau());
                    binding.tvStatus.setText(installation.getStatus());
                    binding.tvType.setText(installation.getType());
                    binding.tvCompensation.setText(installation.getCompensation());
                    binding.tvPower.setText(installation.getPower());

                    // 2. CONTROL DEL SHIMMER (Apagar)
                    binding.shimmerViewContainer.stopShimmer();
                    binding.shimmerViewContainer.setVisibility(View.GONE);
                    binding.contentLayout.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    // También apagamos el shimmer si hay error
                    binding.shimmerViewContainer.stopShimmer();
                    binding.shimmerViewContainer.setVisibility(View.GONE);
                    // Mostrar error
                    binding.tvStatus.setText(errorMessage);

                });
            }
        });
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
