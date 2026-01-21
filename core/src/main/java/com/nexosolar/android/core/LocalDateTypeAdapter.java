package com.nexosolar.android.core;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Adapter personalizado para serializar y deserializar objetos LocalDate con Gson.
 *
 * Permite convertir fechas LocalDate desde/hacia JSON usando el formato dd/MM/yyyy,
 * necesario para la comunicación con la API que espera fechas en formato europeo.
 *
 * Esta clase debe registrarse en el GsonBuilder para ser utilizada automáticamente
 * en todas las operaciones de serialización/deserialización del proyecto.
 */
public class LocalDateTypeAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    // ===== Variables de instancia =====

    /**
     * Formato de fecha utilizado por la API: dd/MM/yyyy (formato europeo)
     */
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== Métodos públicos =====

    /**
     * Convierte un objeto LocalDate a su representación JSON como cadena de texto.
     *
     * @param date Fecha a serializar
     * @param typeOfSrc Tipo del objeto fuente (no utilizado)
     * @param context Contexto de serialización de Gson
     * @return JsonPrimitive con la fecha formateada como dd/MM/yyyy
     */
    @Override
    public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.format(formatter));
    }

    /**
     * Convierte una cadena JSON a un objeto LocalDate.
     *
     * @param json Elemento JSON que contiene la fecha como cadena
     * @param typeOfT Tipo del objeto destino (no utilizado)
     * @param context Contexto de deserialización de Gson
     * @return LocalDate parseado desde el formato dd/MM/yyyy
     * @throws JsonParseException si la cadena no cumple el formato esperado
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return LocalDate.parse(json.getAsString(), formatter);
    }
}
