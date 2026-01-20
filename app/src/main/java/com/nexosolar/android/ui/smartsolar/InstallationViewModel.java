package com.nexosolar.android.ui.smartsolar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.nexosolar.android.domain.models.Installation;
import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.usecase.installation.GetInstallationDetailsUseCase;

/**
 * ViewModel que gestiona el estado y la lógica de negocio relacionada
 * con los datos de instalación solar.
 *
 * Responsabilidades:
 * - Ejecutar casos de uso de dominio
 * - Exponer LiveData para observación desde la UI
 * - Gestionar estados de carga y errores
 */
public class InstallationViewModel extends ViewModel {

    // ===== Variables de instancia =====

    private final GetInstallationDetailsUseCase getInstallationDetailsUseCase;

    private final MutableLiveData<Installation> _installation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    // ===== Constructores =====

    public InstallationViewModel(GetInstallationDetailsUseCase useCase) {
        this.getInstallationDetailsUseCase = useCase;
    }

    // ===== Getters (LiveData expuestos) =====

    public LiveData<Installation> getInstallation() {
        return _installation;
    }

    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    public LiveData<String> getError() {
        return _error;
    }

    // ===== Métodos públicos =====

    /**
     * Solicita los detalles de la instalación mediante el caso de uso correspondiente.
     * Actualiza los LiveData según el resultado (éxito/error).
     */
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
