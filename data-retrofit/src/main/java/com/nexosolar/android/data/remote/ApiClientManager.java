package com.nexosolar.android.data.remote;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nexosolar.android.core.LocalDateTypeAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import co.infinum.retromock.BodyFactory;
import co.infinum.retromock.Retromock;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.nexosolar.android.data.remote.BuildConfig;

public class ApiClientManager {

    private static volatile ApiClientManager instance;

    // Clientes base
    private volatile Retrofit retrofitClient;
    private volatile Retromock retromockClient;

    // INSTANCIAS DE SERVICIOS CACHEADAS
    private volatile ApiService mockApiService;
    private volatile ApiService realApiService;


    private Context applicationContext;

    private ApiClientManager() { }

    public static ApiClientManager getInstance() {
        if (instance == null) {
            synchronized (ApiClientManager.class) {
                if (instance == null) {
                    instance = new ApiClientManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (this.applicationContext == null) {
            this.applicationContext = context.getApplicationContext();
        }
    }

    // --- Métodos Privados para obtener Clientes ---

    private Retrofit getRetrofitClient() {
        if (retrofitClient == null) {
            synchronized (this) {
                if (retrofitClient == null) {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                            .create();
                    retrofitClient = new Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL_2)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofitClient;
    }

    private Retromock getRetromockClient(Context context) {
        if (retromockClient == null) {
            synchronized (this) {
                if (retromockClient == null) {
                    // Asegurar contexto
                    if (applicationContext == null) init(context);

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                            .create();

                    // Usamos la misma configuración de Retrofit base
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.API_BASE_URL_2)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    retromockClient = new Retromock.Builder()
                            .retrofit(retrofit)
                            .defaultBodyFactory(new BodyFactory() {
                                @Override
                                public InputStream create(String input) throws IOException {
                                    return applicationContext.getAssets().open(input);
                                }
                            })
                            .build();
                }
            }
        }
        return retromockClient;
    }

    // --- NUEVO MÉTODO PÚBLICO: Obtener Servicio ---

    /**
     * Devuelve la instancia Singleton del Servicio API.
     * Al reutilizar la instancia, se mantiene el estado (como el contador circular de Retromock).
     */
    public ApiService getApiService(boolean useMock, Context context) {
        if (useMock) {
            if (mockApiService == null) {
                synchronized (this) {
                    if (mockApiService == null) {
                        mockApiService = getRetromockClient(context).create(ApiService.class);
                    }
                }
            }
            return mockApiService;
        } else {
            if (realApiService == null) {
                synchronized (this) {
                    if (realApiService == null) {
                        realApiService = getRetrofitClient().create(ApiService.class);
                    }
                }
            }
            return realApiService;
        }
    }

    public synchronized void reset() {
        retrofitClient = null;
        retromockClient = null;
        mockApiService = null;
        realApiService = null;
    }
}
