package com.nexosolar.android.domain.models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entidad pura de Dominio que representa una factura.
 * Mantiene la independencia de frameworks (sin anotaciones de Room/Retrofit).
 * Incluye lógica de negocio para transformación de estados.
 */
public class Invoice implements Serializable {

    // ===== Variables de instancia =====
    private int id;
    private String descEstado; // Estado en texto crudo (origen API)
    private float importeOrdenacion;
    private LocalDate fecha;

    // ===== Constructores =====

    /**
     * Constructor vacío requerido para herramientas de serialización.
     */
    public Invoice() {
    }

    public Invoice(String descEstado, float importeOrdenacion, LocalDate fecha) {
        this.descEstado = descEstado;
        this.importeOrdenacion = importeOrdenacion;
        this.fecha = fecha;
    }

    // ===== Getters y Setters =====

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

    // ===== Métodos públicos =====

    /**
     * Convierte el estado de texto (API) a un Enum de dominio seguro.
     * Facilita la lógica de UI (colores, iconos) evitando comparaciones de strings.
     *
     * @return Enum {@link InvoiceState} correspondiente o DESCONOCIDO.
     */
    public InvoiceState getEstadoEnum() {
        return InvoiceState.fromTextoServidor(this.descEstado);
    }
}
