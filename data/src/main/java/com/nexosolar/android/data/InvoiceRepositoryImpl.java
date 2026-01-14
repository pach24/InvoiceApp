package com.nexosolar.android.data;

import android.content.Context;
import android.content.SharedPreferences;
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

public class InvoiceRepositoryImpl implements InvoiceRepository {

    private static final String TAG = "DEBUG_REPO";
    private static final String PREFS_NAME = "RepoPrefs";
    private static final String KEY_LAST_MODE_WAS_MOCK = "last_mode_was_mock";

    private final boolean isMockMode;
    private final ApiService apiService;
    private final InvoiceDao invoiceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SharedPreferences prefs;

    public InvoiceRepositoryImpl(boolean useMock, Context context) {

        Log.d(TAG, "=================================================");
        Log.d(TAG, "Inicializando InvoiceRepositoryImpl");
        Log.d(TAG, "Modo solicitado: " + (useMock ? "MOCK" : "REAL"));

        ApiClientManager clientManager = ApiClientManager.getInstance();
        clientManager.init(context);
        this.apiService = clientManager.getApiService(useMock, context);
        this.isMockMode = useMock;

        AppDatabase db = AppDatabase.getInstance(context);
        this.invoiceDao = db.invoiceDao();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


        checkAndClearIfModeChangedSync();

        Log.d(TAG, "Repositorio listo.");
        Log.d(TAG, "=================================================");
    }

    /**
     * VERSIÓN SÍNCRONA del chequeo (se ejecuta en el hilo del constructor)
     * Así nos aseguramos de que termina ANTES de cualquier llamada a getFacturas.
     */
    private void checkAndClearIfModeChangedSync() {
        try {
            // Usamos submit().get() para bloquear hasta que termine la tarea en background
            executor.submit(() -> {
                boolean lastModeWasMock = prefs.getBoolean(KEY_LAST_MODE_WAS_MOCK, false);

                Log.d(TAG, "Último modo guardado: " + (lastModeWasMock ? "MOCK" : "REAL"));
                Log.d(TAG, "Modo actual: " + (isMockMode ? "MOCK" : "REAL"));

                if (isMockMode != lastModeWasMock) {
                    Log.w(TAG, "*** CAMBIO DE MODO DETECTADO ***");
                    Log.w(TAG, "Limpiando base de datos para evitar datos sucios...");

                    // CORRECCIÓN 1: No asignar el resultado a 'int' si devuelve void
                    invoiceDao.deleteAll();

                    Log.w(TAG, "Base de datos limpiada.");

                    // Guardar el nuevo estado
                    prefs.edit().putBoolean(KEY_LAST_MODE_WAS_MOCK, isMockMode).apply();
                    Log.d(TAG, "Preferencia actualizada al modo actual.");
                } else {
                    Log.d(TAG, "Sin cambio de modo. Caché preservada (si existe).");
                    prefs.edit().putBoolean(KEY_LAST_MODE_WAS_MOCK, isMockMode).apply();
                }
            }).get(); // Esto lanza excepciones

            Log.d(TAG, "Sincronización de borrado completada.");

        } catch (Exception e) {
            // Captura InterruptedException y ExecutionException
            Log.e(TAG, "Error crítico sincronizando borrado de BD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void getFacturas(RepositoryCallback<List<Invoice>> callback) {
        Log.d(TAG, "-------------------------------------------------");
        Log.d(TAG, "getFacturas() llamado. Modo: " + (isMockMode ? "MOCK" : "REAL"));

        executor.execute(() -> {
            if (isMockMode) {
                // MODO MOCK: Siempre forzar ciclo circular
                Log.d(TAG, "[MOCK] Borrando caché para avanzar ciclo Retromock...");
                invoiceDao.deleteAll();
                Log.d(TAG, "[MOCK] Solicitando siguiente respuesta del ciclo...");

                refreshFacturas(new RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Log.d(TAG, "[MOCK] Refresh exitoso. Leyendo datos guardados...");
                        readFromDatabase(callback);
                    }
                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "[MOCK] Error en refresh: " + t.getMessage());
                        callback.onError(t);
                    }
                });
            } else {
                // MODO REAL: Cache first
                Log.d(TAG, "[REAL] Consultando caché local...");
                List<InvoiceEntity> entities = invoiceDao.getAllList();

                if (entities != null && !entities.isEmpty()) {
                    Log.d(TAG, "[REAL] Caché encontrada: " + entities.size() + " facturas.");
                    Log.d(TAG, "[REAL] Devolviendo datos locales.");

                    List<Invoice> domainList = new ArrayList<>();
                    for (InvoiceEntity e : entities) domainList.add(e.toDomain());
                    callback.onSuccess(domainList);
                } else {
                    Log.d(TAG, "[REAL] Caché vacía. Descargando desde API...");
                    refreshFacturas(new RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                            Log.d(TAG, "[REAL] Descarga exitosa. Leyendo datos...");
                            readFromDatabase(callback);
                        }
                        @Override
                        public void onError(Throwable t) {
                            Log.e(TAG, "[REAL] Error en descarga: " + t.getMessage());
                            callback.onError(t);
                        }
                    });
                }
            }
        });
    }

    private void readFromDatabase(RepositoryCallback<List<Invoice>> callback) {
        executor.execute(() -> {
            List<InvoiceEntity> entities = invoiceDao.getAllList();
            int count = (entities != null) ? entities.size() : 0;
            Log.d(TAG, "Lectura final de BD: " + count + " registros.");

            List<Invoice> domainList = new ArrayList<>();
            if (entities != null) {
                for (InvoiceEntity e : entities) domainList.add(e.toDomain());
            }
            callback.onSuccess(domainList);
        });
    }

    @Override
    public void refreshFacturas(RepositoryCallback<Boolean> callback) {
        Log.d(TAG, ">>> refreshFacturas: Iniciando petición HTTP...");

        apiService.getFacturas().enqueue(new Callback<InvoiceResponse>() {
            @Override
            public void onResponse(Call<InvoiceResponse> call, Response<InvoiceResponse> response) {
                Log.d(TAG, "<<< HTTP Response: Code " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Invoice> facturasApi = response.body().getFacturas();
                    Log.d(TAG, "Payload recibido: " + facturasApi.size() + " facturas");

                    executor.execute(() -> {
                        try {
                            Log.d(TAG, "Guardando en Room...");
                            invoiceDao.deleteAll();

                            List<InvoiceEntity> entities = new ArrayList<>();
                            for (Invoice i : facturasApi) entities.add(InvoiceEntity.fromDomain(i));
                            invoiceDao.insertAll(entities);

                            Log.d(TAG, "Datos persistidos correctamente.");
                            if (callback != null) callback.onSuccess(true);
                        } catch (Throwable e) {
                            Log.e(TAG, "ERROR guardando en Room: " + e.getMessage());
                            e.printStackTrace();
                            if (callback != null) callback.onError(e);
                        }
                    });
                } else {
                    Log.e(TAG, "Error HTTP: " + response.message());
                    if (callback != null) callback.onSuccess(false);
                }
            }

            @Override
            public void onFailure(Call<InvoiceResponse> call, Throwable t) {
                Log.e(TAG, "FALLO DE RED: " + t.getMessage());
                t.printStackTrace();
                if (callback != null) callback.onError(t);
            }
        });
    }
}
