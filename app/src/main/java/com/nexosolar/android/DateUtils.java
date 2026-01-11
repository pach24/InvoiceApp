package com.nexosolar.android;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

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
}
