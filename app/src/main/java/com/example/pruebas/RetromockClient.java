package com.example.pruebas;


import android.content.Context;
import co.infinum.retromock.Retromock;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetromockClient {
    private static Retromock retromock;

    public static Retromock getClient(Context context) {
        if (retromock == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://mockapi.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            retromock = new Retromock.Builder()
                    .retrofit(retrofit)
                    .defaultBodyFactory(context.getAssets()::open) // Carga desde assets
                    .build();
        }
        return retromock;
    }
}

