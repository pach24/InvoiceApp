package com.nexosolar.android.domain;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.time.LocalDate;

@Entity(tableName = "facturas")
public class Invoice {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String descEstado;
    private float importeOrdenacion;
    private LocalDate fecha;

    // --- GETTERS Y SETTERS NECESARIOS PARA ROOM ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescEstado() {
        return descEstado;
    }

    // ¡ESTE ES EL QUE TE FALTABA!
    public void setDescEstado(String descEstado) {
        this.descEstado = descEstado;
    }

    public float getImporteOrdenacion() {
        return importeOrdenacion;
    }

    // Añadimos este también por si acaso
    public void setImporteOrdenacion(float importeOrdenacion) {
        this.importeOrdenacion = importeOrdenacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    // Añadimos este también por si acaso
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    // --- TUS MÉTODOS AUXILIARES ---

    public InvoiceState getEstadoEnum() {
        return InvoiceState.fromTextoServidor(this.descEstado);
    }
}
