package com.nexosolar.android.data;

import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.remote.InvoiceDto;
import com.nexosolar.android.domain.models.Invoice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper bidireccional entre modelos de dominio y entidades de base de datos.
 *
 * Responsabilidades:
 * - Transformar InvoiceEntity (capa data) ↔ Invoice (capa domain)
 * - Aislar el dominio de detalles de persistencia y red
 * - Permitir evolución independiente de los modelos en cada capa
 *
 * Ubicación en Clean Architecture: reside en la capa data porque necesita
 * conocer tanto los modelos de dominio como las entidades de persistencia,
 * y es responsabilidad de data preparar los datos para el dominio.
 */
public class InvoiceMapper {

    // ===== Mapeo de Entidad a Dominio =====

    /**
     * Convierte una entidad de base de datos a modelo de dominio.
     *
     * @param entity Entidad de Room, o null
     * @return Modelo de dominio, o null si entity es null
     */
    public Invoice toDomain(InvoiceEntity entity) {
        if (entity == null) return null;

        Invoice invoice = new Invoice();
        invoice.setInvoiceStatus(entity.estado);
        invoice.setInvoiceAmount(entity.importe);
        invoice.setInvoiceDate(entity.fecha);
        // invoice.setId(entity.id); // Descomentar si el dominio requiere ID

        return invoice;
    }

    /**
     * Convierte una lista de entidades a lista de modelos de dominio.
     *
     * @param entities Lista de entidades de Room
     * @return Lista de modelos de dominio (vacía si entities es null)
     */
    public List<Invoice> toDomainList(List<InvoiceEntity> entities) {
        List<Invoice> list = new ArrayList<>();
        if (entities != null) {
            for (InvoiceEntity entity : entities) {
                list.add(toDomain(entity));
            }
        }
        return list;
    }

    // ===== Mapeo de Dominio a Entidad =====

    // InvoiceMapper.java
    public InvoiceEntity toEntity(Invoice dto) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.estado = dto.getInvoiceStatus();
        entity.importe = dto.getInvoiceAmount();
        // Aquí conviertes String date (DTO) -> LocalDate (Entity)

        entity.fecha = dto.getInvoiceDate();

        return entity;
    }

    public List<InvoiceEntity> toEntityListFromDto(List<InvoiceDto> dtos) {
        List<InvoiceEntity> list = new ArrayList<>();
        if (dtos != null) {
            for (InvoiceDto dto : dtos) {
                list.add(toEntityFromDto(dto));
            }
        }
        return list;
    }
    private InvoiceEntity toEntityFromDto(InvoiceDto dto) {
        if (dto == null) return null;

        InvoiceEntity entity = new InvoiceEntity();
        entity.estado = dto.status;
        entity.importe = dto.amount;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            entity.fecha = LocalDate.parse(dto.date, formatter);
        } catch (Exception e) {
            // Manejo seguro en caso de error de parsing
            entity.fecha = null; // o LocalDate.now(), según prefieras
        }

        return entity;
    }


}
