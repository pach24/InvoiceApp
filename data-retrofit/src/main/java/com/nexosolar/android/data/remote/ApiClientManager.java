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

    // --- CLIENTES RETROFIT (Transporte) ---
    // Guardamos referencia a los clientes por si necesitamos acceder a ellos directamente
    private volatile Retrofit retrofitClientUrl1; // Para URL Principal
    private volatile Retrofit retrofitClientUrl2; // Para URL Alternativa
    private volatile Retromock retromockClient;

    // --- SERVICIOS API (Endpoints) ---
    // Cacheados por separado para cambio rápido sin reconstrucción
    private volatile ApiService mockApiService;
    private volatile ApiService realApiServiceUrl1;
    private volatile ApiService realApiServiceUrl2;

    private Context applicationContext;

    // Constructor privado (Singleton)
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

    // =================================================================================
    // MÉTODOS PRIVADOS DE CONSTRUCCIÓN (HELPER)
    // =================================================================================

    /**
     * Helper para construir una instancia de Retrofit dada una URL.
     * Evita duplicar código entre el cliente 1 y el cliente 2.
     */
    private Retrofit buildRetrofit(String baseUrl) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                .create();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    /**
     * Obtiene o crea el cliente Retrofit para la URL Principal
     */
    private Retrofit getRetrofitClientUrl1() {
        if (retrofitClientUrl1 == null) {
            synchronized (this) {
                if (retrofitClientUrl1 == null) {
                    retrofitClientUrl1 = buildRetrofit(BuildConfig.API_BASE_URL);
                }
            }
        }
        return retrofitClientUrl1;
    }

    /**
     * Obtiene o crea el cliente Retrofit para la URL Alternativa
     */
    private Retrofit getRetrofitClientUrl2() {
        if (retrofitClientUrl2 == null) {
            synchronized (this) {
                if (retrofitClientUrl2 == null) {
                    retrofitClientUrl2 = buildRetrofit(BuildConfig.API_BASE_URL_2);
                }
            }
        }
        return retrofitClientUrl2;
    }

    private Retromock getRetromockClient(Context context) {
        if (retromockClient == null) {
            synchronized (this) {
                if (retromockClient == null) {
                    if (applicationContext == null) init(context);

                    // Retromock necesita un Retrofit base (usamos la config de la URL 1 como base)
                    Retrofit retrofitBase = getRetrofitClientUrl1();

                    retromockClient = new Retromock.Builder()
                            .retrofit(retrofitBase)
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

    // =================================================================================
    // MÉTODO PÚBLICO PRINCIPAL
    // =================================================================================

    /**
     * Devuelve la instancia correcta del servicio API basándose en la configuración.
     *
     * @param useMock true para usar datos simulados
     * @param useAlternativeUrl true para usar URL2, false para URL1 (Solo si useMock es false)
     * @param context contexto necesario para Retromock
     */
    public ApiService getApiService(boolean useMock, boolean useAlternativeUrl, Context context) {

        // CASO 1: MODO MOCK
        if (useMock) {
            if (mockApiService == null) {
                synchronized (this) {
                    if (mockApiService == null) {
                        mockApiService = getRetromockClient(context).create(ApiService.class);
                    }
                }
            }
            return mockApiService;
        }

        // CASO 2: MODO REAL (URL ALTERNATIVA)
        else if (useAlternativeUrl) {
            if (realApiServiceUrl2 == null) {
                synchronized (this) {
                    if (realApiServiceUrl2 == null) {
                        realApiServiceUrl2 = getRetrofitClientUrl2().create(ApiService.class);
                    }
                }
            }
            return realApiServiceUrl2;
        }

        // CASO 3: MODO REAL (URL PRINCIPAL - DEFAULT)
        else {
            if (realApiServiceUrl1 == null) {
                synchronized (this) {
                    if (realApiServiceUrl1 == null) {
                        realApiServiceUrl1 = getRetrofitClientUrl1().create(ApiService.class);
                    }
                }
            }
            return realApiServiceUrl1;
        }
    }

    /**
     * Resetea todas las conexiones. Útil si se quiere limpiar memoria.
     */
    public synchronized void reset() {
        retrofitClientUrl1 = null;
        retrofitClientUrl2 = null;
        retromockClient = null;

        mockApiService = null;
        realApiServiceUrl1 = null;
        realApiServiceUrl2 = null;
    }
}
