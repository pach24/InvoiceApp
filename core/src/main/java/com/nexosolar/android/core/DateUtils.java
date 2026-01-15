package com.nexosolar.android.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    // Convierte LocalDate a milisegundos (para MaterialDatePicker)
    public static Long localDateToMillis(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    // Convierte milisegundos a LocalDate 
    public static LocalDate millisToLocalDate(Long millis) {
        if (millis == null) return null;
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * Devuelve la fecha con formato "20 Ene 2025".
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"));
        String fechaStr = date.format(formatter);

        // Lógica limpia para capitalizar
        int primerEspacio = fechaStr.indexOf(' ');
        if (primerEspacio != -1 && primerEspacio + 1 < fechaStr.length()) {
            char letraMes = fechaStr.charAt(primerEspacio + 1);
            if (Character.isLowerCase(letraMes)) {
                // Usamos StringBuilder para evitar crear múltiples objetos String innecesarios
                StringBuilder sb = new StringBuilder(fechaStr);
                sb.setCharAt(primerEspacio + 1, Character.toUpperCase(letraMes));
                fechaStr = sb.toString();
            }
        }

        return fechaStr.replace(".", "");
    }
}
