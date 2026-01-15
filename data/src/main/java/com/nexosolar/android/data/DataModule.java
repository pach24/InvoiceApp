package com.nexosolar.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.nexosolar.android.data.local.AppDatabase;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.remote.ApiClientManager;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.repository.InstallationRepositoryImpl;
import com.nexosolar.android.data.repository.InvoiceRepositoryImpl;
import com.nexosolar.android.data.source.InvoiceRemoteDataSource;
import com.nexosolar.android.data.source.InvoiceRemoteDataSourceImpl;
import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.repository.InvoiceRepository;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class DataModule {

    private final Context context;
    private final boolean useMock;

    // Claves para SharedPreferences
    private static final String PREFS_NAME = "RepoPrefs";
    private static final String KEY_LAST_MODE_WAS_MOCK = "last_mode_was_mock";

    public DataModule(Context context, boolean useMock) {
        this.context = context.getApplicationContext();
        this.useMock = useMock;
    }

    public InvoiceRepository provideInvoiceRepository() {
        ApiService apiService = provideApiService();
        InvoiceDao invoiceDao = provideInvoiceDao();
        SharedPreferences prefs = provideSharedPrefs();

        // 1. Limpieza de base de datos si cambia el modo (igual que antes)
        boolean lastModeWasMock = prefs.getBoolean(KEY_LAST_MODE_WAS_MOCK, false);

        if (this.useMock != lastModeWasMock) {
            try {
                Executors.newSingleThreadExecutor().submit(() -> {
                    invoiceDao.deleteAll();
                }).get();
                prefs.edit().putBoolean(KEY_LAST_MODE_WAS_MOCK, this.useMock).apply();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        InvoiceRemoteDataSource remoteDataSource = new InvoiceRemoteDataSourceImpl(apiService);

        // 2. CORRECCIÓN AQUÍ:
        // Pasamos 'this.useMock' como tercer parámetro.
        // Si es true (Mock), el repositorio siempre forzará la recarga (activando la circularidad).
        // Si es false (Real), el repositorio usará caché primero.
        return new InvoiceRepositoryImpl(remoteDataSource, invoiceDao, this.useMock);
    }

    public InstallationRepository provideInstallationRepository() {
        return new InstallationRepositoryImpl(provideApiService());
    }

    // --- Proveedores Privados (Singletons o Helpers) ---

    private ApiService provideApiService() {
        // Inicializamos el Manager si no lo estaba
        ApiClientManager.getInstance().init(context);
        // ApiClientManager devuelve la instancia Mock o Real según el booleano
        return ApiClientManager.getInstance().getApiService(useMock, context);
    }

    private InvoiceDao provideInvoiceDao() {
        // Obtenemos la instancia singleton de la BD y devolvemos el DAO
        return AppDatabase.getInstance(context).invoiceDao();
    }

    private SharedPreferences provideSharedPrefs() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
