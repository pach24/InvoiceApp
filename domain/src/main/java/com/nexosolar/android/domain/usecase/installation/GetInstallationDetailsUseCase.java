package com.nexosolar.android.domain.usecase.installation;

import com.nexosolar.android.domain.repository.InstallationRepository;

/**
 * Caso de uso para recuperar los detalles técnicos de la instalación asociada al usuario.
 * Coordina la llamada al repositorio y proporciona un punto centralizado para extender
 * lógica de negocio (validaciones, transformaciones, auditoría) sin afectar la capa de presentación.
 */
public class GetInstallationDetailsUseCase {

    // ===== Variables de instancia =====
    private final InstallationRepository repository;

    // ===== Constructores =====
    public GetInstallationDetailsUseCase(InstallationRepository repository) {
        this.repository = repository;
    }


    // ===== Métodos públicos =====

    /**
     * Ejecuta la obtención de detalles de instalación.
     * Actualmente delega directamente al repositorio, pero permite extender
     * con validaciones previas o transformaciones posteriores.
     */
    public void execute(InstallationRepository.InstallationCallback callback) {
        // Aquí podrías añadir lógica de negocio antes/después de llamar al repo
        // Por ejemplo: validaciones, logging, caché, etc.
        repository.getInstallationDetails(callback);
    }
}
