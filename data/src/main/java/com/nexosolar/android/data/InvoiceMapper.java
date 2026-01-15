package com.nexosolar.android.data;

import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.domain.models.Invoice;

import java.util.ArrayList;
import java.util.List;

public class InvoiceMapper {

    // De Entidad (BD) a Dominio
    public Invoice toDomain(InvoiceEntity entity) {
        if (entity == null) return null;

        Invoice invoice = new Invoice();
        // Usamos los campos públicos directos ya que no tienes getters en la Entity
        invoice.setDescEstado(entity.estado);
        invoice.setImporteOrdenacion(entity.importe);
        invoice.setFecha(entity.fecha);
        // invoice.setId(entity.id); // Descomenta si tu modelo de dominio tiene ID

        return invoice;
    }

    public List<Invoice> toDomainList(List<InvoiceEntity> entities) {
        List<Invoice> list = new ArrayList<>();
        if (entities != null) {
            for (InvoiceEntity entity : entities) {
                list.add(toDomain(entity));
            }
        }
        return list;
    }

    // De Dominio a Entidad (BD)
    public InvoiceEntity toEntity(Invoice domain) {
        if (domain == null) return null;

        InvoiceEntity entity = new InvoiceEntity();
        entity.estado = domain.getDescEstado();
        entity.importe = domain.getImporteOrdenacion();
        entity.fecha = domain.getFecha();

        return entity;
    }

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
