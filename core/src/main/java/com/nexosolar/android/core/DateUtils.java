package com.nexosolar.android.core;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Clase de utilidad para operaciones comunes con fechas en la aplicación.
 *
 * Proporciona métodos para formatear fechas en distintos formatos según el contexto de uso,
 * y para realizar conversiones entre LocalDate y milisegundos (timestamp) necesarias para
 * la integración con componentes nativos de Android como MaterialDatePicker.
 *
 * Todos los métodos son estáticos y seguros ante valores null.
 */
public class DateUtils {

    // ===== Variables de instancia =====

    /**
     * Formateador para fechas cortas en formato europeo: dd/MM/yy
     * Usado en componentes de UI donde el espacio es limitado.
     */
    private static final DateTimeFormatter SHORT_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault());

    // ===== Métodos públicos =====

    /**
     * Formatea una fecha en formato corto: dd/MM/yy
     *
     * @param date Fecha a formatear
     * @return Cadena con la fecha formateada, o cadena vacía si date es null
     */
    public static String formatDateShort(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(SHORT_FORMATTER);
    }


    /**
     * Formatea una fecha en formato de texto legible para el usuario: "20 Ene 2025"
     * Aplica locale español (es-ES) y capitaliza el mes correctamente.
     *
     * Se utiliza principalmente en vistas de facturas, informes y otros documentos
     * donde se requiere un formato de fecha más formal y legible.
     *
     * @param date Fecha a formatear
     * @return Cadena con la fecha formateada, o cadena vacía si date es null
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.forLanguageTag("es-ES"));
        String fechaStr = date.format(formatter);

        // Capitaliza la primera letra del mes (ej: "ene" → "Ene")
        int primerEspacio = fechaStr.indexOf(' ');
        if (primerEspacio != -1 && primerEspacio + 1 < fechaStr.length()) {
            char letraMes = fechaStr.charAt(primerEspacio + 1);
            if (Character.isLowerCase(letraMes)) {
                StringBuilder sb = new StringBuilder(fechaStr);
                sb.setCharAt(primerEspacio + 1, Character.toUpperCase(letraMes));
                fechaStr = sb.toString();
            }
        }

        // Elimina puntos que puedan aparecer después de las abreviaciones de meses
        return fechaStr.replace(".", "");
    }
}
