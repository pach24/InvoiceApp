package com.nexosolar.android.domain;

import com.nexosolar.android.domain.Installation;
import com.nexosolar.android.domain.InstallationRepository;

/**
 * Caso de uso que encapsula la lógica de negocio para obtener detalles de instalación.
 * Puede incluir validaciones, transformaciones, etc.
 */
public class GetInstallationDetailsUseCase {

    private final InstallationRepository repository;

    public GetInstallationDetailsUseCase(InstallationRepository repository) {
        this.repository = repository;
    }

    /**
     * Ejecuta el caso de uso
     */
    public void execute(InstallationRepository.InstallationCallback callback) {
        // Aquí podrías añadir lógica de negocio antes/después de llamar al repo
        // Por ejemplo: validaciones, logging, caché, etc.
        repository.getInstallationDetails(callback);
    }
}
