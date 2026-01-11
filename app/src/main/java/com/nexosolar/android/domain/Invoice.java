package com.nexosolar.android.domain;

import java.time.LocalDate;



public class Invoice {
    private String descEstado;
    private float importeOrdenacion;
    private LocalDate fecha;

    public InvoiceState getEstadoEnum() {
        return InvoiceState.fromTextoServidor(this.descEstado);
    }

    public String getDescEstado() {
        return descEstado;
    }
    public float getImporteOrdenacion() {
        return importeOrdenacion;
    }
    public LocalDate getFecha() {
        return fecha;
    }



}
