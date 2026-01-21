package com.nexosolar.android.domain.repository;

import com.nexosolar.android.domain.models.Installation;

/**
 * Contrato de repositorio para la gestión de datos de instalaciones.
 * Abstrae la fuente de datos (API/Cache) de la capa de dominio.
 */
public interface InstallationRepository {

    // ===== Métodos públicos =====

    /**
     * Recupera los detalles técnicos de la instalación asociada al usuario.
     * @param callback Mecanismo de retorno asíncrono.
     */
    void getInstallationDetails(InstallationCallback callback);

    // ===== Interfaces internas =====

    /**
     * Contrato específico para la respuesta de detalles de instalación.
     * Permite manejar casos de éxito y error de forma tipada.
     */
    interface InstallationCallback {
        void onSuccess(Installation installation);
        void onError(String errorMessage);
    }
}
