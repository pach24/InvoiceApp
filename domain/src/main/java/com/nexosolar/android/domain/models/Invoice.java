package com.nexosolar.android.domain.models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entidad de Dominio Pura.
 * No contiene anotaciones de Android (Room, Retrofit, etc.).
 * Representa una factura en la lógica de negocio.
 */
public class Invoice implements Serializable {

    // Identificador (opcional en dominio, pero útil si lo necesitas para navegar a detalles)
    private int id;

    private String descEstado;
    private float importeOrdenacion;
    private LocalDate fecha;

    // --- CONSTRUCTORES ---

    public Invoice() {
        // Constructor vacío requerido por muchas herramientas de serialización
    }

    public Invoice(String descEstado, float importeOrdenacion, LocalDate fecha) {
        this.descEstado = descEstado;
        this.importeOrdenacion = importeOrdenacion;
        this.fecha = fecha;
    }

    // --- GETTERS Y SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescEstado() {
        return descEstado;
    }

    public void setDescEstado(String descEstado) {
        this.descEstado = descEstado;
    }

    public float getImporteOrdenacion() {
        return importeOrdenacion;
    }

    public void setImporteOrdenacion(float importeOrdenacion) {
        this.importeOrdenacion = importeOrdenacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }


    // --- NUEVO (LÓGICA DE DOMINIO) ---
    /**
     * Convierte el texto del servidor a un Enum para que la UI trabaje
     * con tipos seguros en lugar de Strings.
     *
     * @return El estado como Enum InvoiceState
     */
    public InvoiceState getEstadoEnum() {
        return InvoiceState.fromTextoServidor(this.descEstado);
    }

}
