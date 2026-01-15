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

}
