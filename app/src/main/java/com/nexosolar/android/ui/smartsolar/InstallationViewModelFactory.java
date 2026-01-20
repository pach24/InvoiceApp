package com.nexosolar.android.ui.smartsolar;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.nexosolar.android.data.DataModule;
import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;

/**
 * Factory para la creación de InstallationViewModel con inyección manual de dependencias.
 *
 * Responsabilidades:
 * - Construir la cadena de dependencias (Repository -> UseCase -> ViewModel)
 * - Permitir alternar entre datos reales y mock mediante flag useMock
 *
 *
 */
public class InstallationViewModelFactory implements ViewModelProvider.Factory {

    // ===== Variables de instancia =====

    private final Context context;
    private final boolean useMock;

    // ===== Constructores =====

    public InstallationViewModelFactory(Context context, boolean useMock) {
        this.context = context.getApplicationContext();
        this.useMock = useMock;
    }

    // ===== Métodos públicos =====

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(InstallationViewModel.class)) {
            DataModule dataModule = new DataModule(context, useMock);
            InstallationRepository repository = dataModule.provideInstallationRepository();
            GetInstallationDetailsUseCase useCase = new GetInstallationDetailsUseCase(repository);
            return (T) new InstallationViewModel(useCase);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
