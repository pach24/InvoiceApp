package com.example.pruebas;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class Invoice {
    private String descEstado;
    private float importeOrdenacion;
    private String fecha;

    public String getDescEstado() {
        return descEstado;
    }
    public float getImporteOrdenacion() {
        return importeOrdenacion;
    }
    public String getFecha() {
        return fecha; }

    public static Date stringToDate(String fechaString) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");  // Ajusta el formato de acuerdo a tu fecha
        try {
            return formatter.parse(fechaString);  // Convierte el String a Date
        } catch (ParseException e) {

            return null;
        }
    }

}
