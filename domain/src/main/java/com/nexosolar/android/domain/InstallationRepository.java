package com.nexosolar.android.domain;

import com.nexosolar.android.domain.Installation;

/**
 * Contrato para obtener datos de instalación solar.
 * La implementación concreta estará en la capa de datos.
 */
public interface InstallationRepository {
    /**
     * Obtiene los detalles de la instalación.
     * @param callback Callback para manejar respuesta o error
     */
    void getInstallationDetails(InstallationCallback callback);

    /**
     * Callback para respuesta asíncrona
     */
    interface InstallationCallback {
        void onSuccess(Installation installation);
        void onError(String errorMessage);
    }
}
