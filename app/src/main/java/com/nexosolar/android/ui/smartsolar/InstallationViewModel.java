package com.nexosolar.android.ui.smartsolar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nexosolar.android.domain.models.Installation;
import com.nexosolar.android.domain.repository.InstallationRepository; // Solo la interfaz (opcional, si el callback lo requiere)
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;

public class InstallationViewModel extends ViewModel {

    private final GetInstallationDetailsUseCase getInstallationDetailsUseCase;

    // LiveData
    private final MutableLiveData<Installation> _installation = new MutableLiveData<>();
    public LiveData<Installation> getInstallation() { return _installation; }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() { return _isLoading; }

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> getError() { return _error; }

    public InstallationViewModel(GetInstallationDetailsUseCase useCase) {
        this.getInstallationDetailsUseCase = useCase;
    }

    public void loadInstallationDetails() {
        _isLoading.setValue(true);

        getInstallationDetailsUseCase.execute(new InstallationRepository.InstallationCallback() {
            @Override
            public void onSuccess(Installation installation) {
                _isLoading.postValue(false);
                _installation.postValue(installation);
            }

            @Override
            public void onError(String errorMessage) {
                _isLoading.postValue(false);
                _error.postValue(errorMessage);
            }
        });
    }
}
