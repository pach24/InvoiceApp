package com.nexosolar.android.core;

import java.time.LocalDate;

/**
 * Validador simple y enfocado solo en validación de fechas.
 * Principio: Single Responsibility (SRP). Solo sabe validar fechas.
 */
public class DateValidator {

    /**
     * Valida coherencia de rango de fechas (permite nulos).
     * @return true si el rango es coherente o si alguno es null.
     */
    public static boolean isValidRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return true;
        return !start.isAfter(end);
    }

    /**
     * Valida que una fecha esté entre límites (inclusive).
     */
    public static boolean isWithinBounds(LocalDate date, LocalDate min, LocalDate max) {
        if (date == null) return true;
        boolean afterMin = min == null || !date.isBefore(min);
        boolean beforeMax = max == null || !date.isAfter(max);
        return afterMin && beforeMax;
    }
}