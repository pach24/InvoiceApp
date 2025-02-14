package com.example.pruebas;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Devuelve una instancia de RetroFit con la URL base de la api
public class RetroFitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "https://viewnextandroid.wiremockapi.cloud/";

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            //Crea una instancia de RetroFit además de convertir el json a nuestro objeto
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }
}
