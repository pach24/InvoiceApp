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

/**
 * Singleton que gestiona las instancias de Retrofit (API real) y Retromock (API mock).
 * Garantiza una única instancia de cada cliente durante toda la vida de la aplicación.
 */
public class ApiClientManager {

    // Instancia única del Singleton
    private static volatile ApiClientManager instance;

    // Instancias de los clientes
    private volatile Retrofit retrofitClient;
    private volatile Retromock retromockClient;

    // Base URL de la API real
    private static final String BASE_URL = "https://francisco-pacheco.com/api/";

    // Contexto de aplicación necesario para Retromock
    private Context applicationContext;

    /**
     * Constructor privado para prevenir instanciación externa
     */
    private ApiClientManager() {
        // Constructor privado para Singleton
    }

    /**
     * Obtiene la instancia única del ApiClientManager (Thread-safe con Double-Checked Locking)
     * @return Instancia única del manager
     */
    public static ApiClientManager getInstance() {
        if (instance == null) { // Primera verificación sin sincronización (optimización)
            synchronized (ApiClientManager.class) {
                if (instance == null) { // Segunda verificación con sincronización (thread-safety)
                    instance = new ApiClientManager();
                }
            }
        }
        return instance;
    }

    /**
     * Inicializa el contexto de aplicación (llamar desde Application o MainActivity)
     * @param context Contexto de aplicación
     */
    public void init(Context context) {
        this.applicationContext = context.getApplicationContext();
    }

    /**
     * Obtiene la instancia de Retrofit (API real) - Thread-safe con Double-Checked Locking
     * @return Instancia única de Retrofit
     */
    public Retrofit getRetrofitClient() {
        if (retrofitClient == null) { // Primera verificación
            synchronized (this) {
                if (retrofitClient == null) { // Segunda verificación
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                            .create();

                    retrofitClient = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofitClient;
    }

    /**
     * Obtiene la instancia de Retromock (API mock) - Thread-safe con Double-Checked Locking
     * @param context Contexto necesario para acceder a assets/
     * @return Instancia única de Retromock
     */
    public Retromock getRetromockClient(Context context) {
        if (retromockClient == null) { // Primera verificación
            synchronized (this) {
                if (retromockClient == null) { // Segunda verificación
                    if (applicationContext == null) {
                        applicationContext = context.getApplicationContext();
                    }

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
                            .create();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
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

    /**
     * Reinicia ambas instancias (útil para testing o cambios de configuración)
     */
    public synchronized void reset() {
        retrofitClient = null;
        retromockClient = null;
    }
}
