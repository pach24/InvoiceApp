package com.nexosolar.android.data;

import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.domain.models.Invoice;
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
        invoice.setDescEstado(entity.estado);
        invoice.setImporteOrdenacion(entity.importe);
        invoice.setFecha(entity.fecha);
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

    /**
     * Convierte un modelo de dominio a entidad de base de datos.
     *
     * Usado al persistir datos provenientes de la API (después de mapear
     * desde InvoiceResponse) o al guardar cambios locales.
     *
     * @param domain Modelo de dominio, o null
     * @return Entidad de Room, o null si domain es null
     */
    public InvoiceEntity toEntity(Invoice domain) {
        if (domain == null) return null;

        InvoiceEntity entity = new InvoiceEntity();
        entity.estado = domain.getDescEstado();
        entity.importe = domain.getImporteOrdenacion();
        entity.fecha = domain.getFecha();

        return entity;
    }

    /**
     * Convierte una lista de modelos de dominio a lista de entidades.
     *
     * @param domains Lista de modelos de dominio
     * @return Lista de entidades de Room (vacía si domains es null)
     */
    public List<InvoiceEntity> toEntityList(List<Invoice> domains) {
        List<InvoiceEntity> list = new ArrayList<>();
        if (domains != null) {
            for (Invoice domain : domains) {
                list.add(toEntity(domain));
            }
        }
        return list;
    }
}
