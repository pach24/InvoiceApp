package com.nexosolar.android.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.nexosolar.android.domain.models.Invoice;
import java.time.LocalDate;

@Entity(tableName = "facturas")
public class InvoiceEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public float importe;
    public String estado;
    public LocalDate fecha; // Requiere TypeConverters en AppDatabase

    public InvoiceEntity() {
    }

    // --- MAPPER: De Entity (BD) a Domain (Lógica) ---
    public Invoice toDomain() {
        Invoice invoice = new Invoice();
        // invoice.setId(this.id); // Si tu dominio tiene ID, úsalo
        invoice.setDescEstado(this.estado);
        invoice.setImporteOrdenacion(this.importe);
        invoice.setFecha(this.fecha);
        return invoice;
    }

    // --- MAPPER: De Domain (Lógica) a Entity (BD) ---
    public static InvoiceEntity fromDomain(Invoice invoice) {
        InvoiceEntity entity = new InvoiceEntity();
        entity.estado = invoice.getDescEstado();
        entity.importe = invoice.getImporteOrdenacion();
        entity.fecha = invoice.getFecha();
        return entity;
    }
}
