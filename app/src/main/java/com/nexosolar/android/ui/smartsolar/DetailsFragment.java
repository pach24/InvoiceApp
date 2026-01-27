package com.nexosolar.android.ui.smartsolar;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.nexosolar.android.R;
import com.nexosolar.android.databinding.FragmentDetailsBinding;
import com.nexosolar.android.NexoSolarApplication;
import com.nexosolar.android.domain.repository.InstallationRepository;


/**
 * Fragment que muestra información detallada de la instalación solar.
 * Responsabilidades:
 * - Consumir datos del ViewModel mediante LiveData
 * - Gestionar estados de carga con Shimmer
 * - Mostrar diálogo informativo sobre los detalles de la instalación
 */
public class DetailsFragment extends Fragment {

    // ===== Variables de instancia =====

    private FragmentDetailsBinding binding;
    private InstallationViewModel viewModel;

    // ===== Métodos del ciclo de vida =====

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        observeLoadingState();
        observeInstallationData();
        observeErrors();
        loadDataIfNeeded();
        setupInfoButton();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ===== Métodos privados =====

    /**
     * Inicializa el ViewModel con su factory.
     * TODO: Posible mejora: Reemplazar useMock=true por inyección de dependencias cuando esté disponible.
     */
    private void setupViewModel() {
        // 1. Obtener la App (contexto global) de forma segura
        NexoSolarApplication app = (NexoSolarApplication) requireActivity().getApplication();

        // 2. Pedir el repositorio al DataModule global
        // Esto respeta AUTOMÁTICAMENTE la configuración (Mock, URL, etc.) que elegiste en MainActivity
        InstallationRepository repository = app.getDataModule().provideInstallationRepository();

        // 3. Crear el Factory inyectando solo el repositorio
        InstallationViewModelFactory factory = new InstallationViewModelFactory(repository);

        // 4. Obtener el ViewModel
        viewModel = new ViewModelProvider(this, factory).get(InstallationViewModel.class);
    }


    /**
     * Observa el estado de carga para mostrar/ocultar el efecto Shimmer.
     */
    private void observeLoadingState() {
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
    }

    /**
     * Observa los datos de instalación y actualiza la UI cuando están disponibles.
     */
    private void observeInstallationData() {
        viewModel.getInstallation().observe(getViewLifecycleOwner(), installation -> {
            if (installation != null) {
                binding.tvCau.setText(installation.getSelfConsumptionCode());
                binding.tvStatus.setText(installation.getInstallationStatus());
                binding.tvType.setText(installation.getInstallationType());
                binding.tvCompensation.setText(installation.getCompensation());
                binding.tvPower.setText(installation.getPower());
            }
        });
    }

    /**
     * Observa errores del ViewModel y notifica al usuario.
     */
    private void observeErrors() {
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Solicita la carga de datos solo si no están previamente disponibles.
     * Evita recargas innecesarias durante rotaciones de pantalla.
     */
    private void loadDataIfNeeded() {
        if (viewModel.getInstallation().getValue() == null) {
            viewModel.loadInstallationDetails();
        }
    }

    /**
     * Configura el listener del botón de información.
     */
    private void setupInfoButton() {
        binding.ivInfo.setOnClickListener(v -> showInfoDialog());
    }

    /**
     * Muestra un diálogo modal con información adicional sobre los detalles mostrados.
     * Configura fondo transparente para respetar las esquinas redondeadas del diseño.
     */
    private void showInfoDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_info, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnAceptar = dialogView.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
