package com.nexosolar.android.ui.smartsolar;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.data.DataModule;
import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;

public class InstallationViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final boolean useMock;

    public InstallationViewModelFactory(Context context, boolean useMock) {
        this.context = context.getApplicationContext();
        this.useMock = useMock;
    }

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
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
