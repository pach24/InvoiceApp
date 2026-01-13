package com.nexosolar.android.data.remote;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nexosolar.android.core.LocalDateTypeAdapter;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.time.LocalDate;

// Devuelve una instancia de RetroFit con la URL base de la api
public class RetroFitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://francisco-pacheco.com/api/";

    // QUITAMOS "Context context" porque no se usa
    public static Retrofit getClient() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}