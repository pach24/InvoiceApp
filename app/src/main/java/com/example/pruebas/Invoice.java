package com.example.pruebas;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
