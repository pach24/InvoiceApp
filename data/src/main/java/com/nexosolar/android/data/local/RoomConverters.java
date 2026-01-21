package com.nexosolar.android.data.local;

import androidx.room.TypeConverter;
import java.time.LocalDate;

/**
 * Convertidores de tipos personalizados para Room Database.
 *
 * Permite que Room maneje tipos Java 8+ como LocalDate convirtiéndolos
 * a tipos primitivos compatibles con SQLite (Long en este caso).
 *
 * Utiliza toEpochDay() para convertir LocalDate en días desde la época Unix,
 * lo cual es más eficiente que almacenar cadenas formateadas y permite
 * realizar comparaciones y ordenamientos directamente en SQL.
 */
public class RoomConverters {

    /**
     * Convierte un timestamp (días desde época) a LocalDate.
     *
     * @param value Número de días desde 1970-01-01, o null
     * @return LocalDate correspondiente, o null si value es null
     */
    @TypeConverter
    public static LocalDate fromTimestamp(Long value) {
        return value == null ? null : LocalDate.ofEpochDay(value);
    }

    /**
     * Convierte un LocalDate a timestamp (días desde época).
     *
     * @param date Fecha a convertir, o null
     * @return Número de días desde 1970-01-01, o null si date es null
     */
    @TypeConverter
    public static Long dateToTimestamp(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }
}
