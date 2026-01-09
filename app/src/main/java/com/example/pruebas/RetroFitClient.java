package com.example.pruebas;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.time.LocalDate;

// Devuelve una instancia de RetroFit con la URL base de la api
public class RetroFitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://viewnextandroid.wiremockapi.cloud/";

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            // 1. Configuramos Gson con el adaptador personalizado para LocalDate
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                    .create();

            // 2. Pasamos esa instancia configurada de Gson al converter
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
