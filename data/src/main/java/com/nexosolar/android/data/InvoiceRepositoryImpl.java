package com.nexosolar.android.data;

import android.content.Context;
import android.util.Log;
import com.nexosolar.android.data.local.AppDatabase;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.local.InvoiceEntity;
import com.nexosolar.android.data.remote.ApiClientManager;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.remote.InvoiceResponse;
import com.nexosolar.android.domain.Invoice;
import com.nexosolar.android.domain.InvoiceRepository;
import com.nexosolar.android.domain.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementación del repositorio de facturas que maneja la lógica de datos.
 * Utiliza ApiClientManager (Singleton) para obtener instancias de Retrofit o Retromock.
 */
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final ApiService apiService;
    private final InvoiceDao invoiceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Constructor que inicializa el repositorio con el cliente adecuado (Mock o Real).
     *
     * @param useMock true para usar Retromock (datos simulados), false para API real
     * @param context Contexto de aplicación necesario para Room y Retromock
     */
    public InvoiceRepositoryImpl(boolean useMock, Context context) {
        // Obtener el Singleton del ApiClientManager
        ApiClientManager clientManager = ApiClientManager.getInstance();

        // Inicializar el contexto en el manager (si no se hizo antes)
        clientManager.init(context);

        // Inicializar API o Mock según el flag usando el Singleton
        if (useMock) {
            this.apiService = clientManager
                    .getRetromockClient(context)
                    .create(ApiService.class);
            Log.d("DEBUG_REPO", "Repositorio inicializado en modo MOCK (Singleton)");
        } else {
            this.apiService = clientManager
                    .getRetrofitClient()
                    .create(ApiService.class);
            Log.d("DEBUG_REPO", "Repositorio inicializado en modo REAL API (Singleton)");
        }

        // Inicializar siempre la base de datos local (Room también usa Singleton)
        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
    }

    /**
     * Obtiene las facturas desde la base de datos local (Room).
     * Este método es síncrono respecto a Room pero devuelve los datos mediante callback.
     *
     * @param callback Callback con la lista de facturas o error
     */
    @Override
    public void getFacturas(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            try {
                Log.d("DEBUG_REPO", "Consultando base de datos local...");

                // 1. Obtener datos de la DB
                List<InvoiceEntity> entities = invoiceDao.getAllList();

                // 2. Mapear Entity -> Domain
                List<Invoice> domainList = new ArrayList<>();
                if (entities != null) {
                    for (InvoiceEntity entity : entities) {
                        domainList.add(entity.toDomain());
                    }
                    Log.d("DEBUG_REPO", "Datos locales encontrados: " + entities.size());
                } else {
                    Log.d("DEBUG_REPO", "Base de datos local vacía (null)");
                }

                // 3. Devolver datos al callback
                callback.onSuccess(domainList);

            } catch (Throwable e) {
                Log.e("DEBUG_REPO", "Error leyendo DB: " + e.getMessage());
                e.printStackTrace();
                callback.onError(e);
            }
        });
    }

    /**
     * Refresca las facturas desde la API (Real o Mock según configuración).
     * Descarga los datos y los guarda en la base de datos local.
     *
     * @param callback Callback que indica si la operación fue exitosa
     */
    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        Log.d("DEBUG_REPO", "Iniciando refreshFacturas (Llamada a API)...");

        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                Log.d("DEBUG_REPO", "Respuesta API recibida. Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Invoice> facturasApi = response.body().getFacturas();
                    Log.d("DEBUG_REPO", "Facturas descargadas: " + facturasApi.size());

                    // Guardar en DB en hilo secundario
                    executor.execute(() -> {
                        try {
                            Log.d("DEBUG_REPO", "Guardando datos en Room...");

                            // Mapear Domain (API) -> Entity (BD)
                            List<InvoiceEntity> entitiesToSave = new ArrayList<>();
                            for (Invoice inv : facturasApi) {
                                entitiesToSave.add(InvoiceEntity.fromDomain(inv));
                            }

                            // Reemplazar datos en BD (borrar todo e insertar nuevos)
                            invoiceDao.deleteAll();
                            invoiceDao.insertAll(entitiesToSave);

                            Log.d("DEBUG_REPO", "Datos guardados correctamente en Room.");

                            // Avisar que terminó correctamente
                            if (callback != null) {
                                callback.onSuccess(true);
                            }

                        } catch (Throwable e) {
                            Log.e("DEBUG_REPO", "Error guardando en DB: " + e.getMessage());
                            e.printStackTrace();
                            if (callback != null) {
                                callback.onError(e);
                            }
                        }
                    });

                } else {
                    Log.e("DEBUG_REPO", "Error API: " + response.message());
                    // La API respondió pero con error (ej. 404, 500)
                    if (callback != null) {
                        callback.onSuccess(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                Log.e("DEBUG_REPO", "Fallo total de red: " + t.getMessage());
                t.printStackTrace();

                // Error de red (sin conexión, timeout, etc.)
                if (callback != null) {
                    callback.onError(t);
                }
            }
        });
    }
}
