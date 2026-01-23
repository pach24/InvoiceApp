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
    private int invoiceID;
    private String invoiceStatus;
    private float invoiceAmount;



    private LocalDate invoiceDate;

    // ===== Constructores =====

    /**
     * Constructor vacío requerido para herramientas de serialización.
     */
    public Invoice() {
    }

    public Invoice(String descEstado, float amountOrder, LocalDate invoiceDate) {
        this.invoiceStatus = descEstado;
        this.invoiceAmount = amountOrder;
        this.invoiceDate = invoiceDate;
    }

    // ===== Getters y Setters =====

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public float getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(float invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    // ===== Métodos públicos =====

    /**
     * Convierte el estado de texto (API) a un Enum de dominio seguro.
     * Facilita la lógica de UI (colores, iconos) evitando comparaciones de strings.
     *
     * @return Enum {@link InvoiceState} correspondiente o DESCONOCIDO.
     */
    public InvoiceState getEstadoEnum() {
        return InvoiceState.fromServerValue(this.invoiceStatus);
    }
}
