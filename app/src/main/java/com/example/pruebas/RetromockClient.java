package com.example.pruebas;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import co.infinum.retromock.BodyFactory;
import co.infinum.retromock.Retromock;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetromockClient {

    private static Retromock retromock;

    public static Retromock getClient(final Context context) {
        if (retromock == null) {
            // 1. Gson con adaptador de LocalDate
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                    .create();

            // 2. Retrofit base
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://viewnextandroid.wiremockapi.cloud/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            // 3. Retromock con BodyFactory compatible (sin lambdas ::)
            retromock = new Retromock.Builder()
                    .retrofit(retrofit)
                    .defaultBodyFactory(new BodyFactory() {
                        @Override
                        public InputStream create(String input) throws IOException {
                            return context.getAssets().open(input);
                        }
                    })
                    .build();
        }
        return retromock;
    }
}
