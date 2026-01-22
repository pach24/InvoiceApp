package com.nexosolar.android.data;

import com.nexosolar.android.data.remote.InstallationDTO;
import com.nexosolar.android.domain.models.Installation;

/**
 * Mapper para convertir InstallationDTO (capa data) a Installation (capa domain)
 * y aislar el dominio de los nombres de atributos del JSON/API.
 */
public class InstallationMapper {

    /**
     * Convierte un DTO de instalación en un modelo de dominio.
     *
     * @param dto DTO recibido desde la API
     * @return Installation del dominio
     */
    public static Installation toDomain(InstallationDTO dto) {
        if (dto == null) return null;

        Installation installation = new Installation();
        installation.setSelfConsumptionCode(dto.cau);
        installation.setInstallationStatus(dto.status);
        installation.setInstallationType(dto.type);
        installation.setCompensation(dto.compensation);
        installation.setPower(dto.power);

        return installation;
    }
}
