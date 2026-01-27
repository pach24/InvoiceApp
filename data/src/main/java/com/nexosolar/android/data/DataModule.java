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

    private static final String PREFS_NAME = "RepoPrefs";
    private static final String KEY_LAST_MODE_WAS_MOCK = "last_mode_was_mock";
    private static final String KEY_LAST_URL_WAS_ALT = "last_url_was_alt"; // Nueva Key

    private final Context context;
    private final boolean useMock;
    private final boolean useAlternativeUrl; // Nuevo Flag

    // Constructor actualizado
    public DataModule(Context context, boolean useMock, boolean useAlternativeUrl) {
        this.context = context.getApplicationContext();
        this.useMock = useMock;
        this.useAlternativeUrl = useAlternativeUrl;
    }

    public InvoiceRepository provideInvoiceRepository() {
        ApiService apiService = provideApiService();
        InvoiceDao invoiceDao = provideInvoiceDao();
        SharedPreferences prefs = provideSharedPrefs();

        // Detectar cualquier cambio de configuración (Mock o URL)
        boolean lastModeWasMock = prefs.getBoolean(KEY_LAST_MODE_WAS_MOCK, false);
        boolean lastUrlWasAlt = prefs.getBoolean(KEY_LAST_URL_WAS_ALT, false);

        // Si cambió el modo Mock O si cambió la URL (estando en modo real)
        boolean configChanged = (this.useMock != lastModeWasMock) ||
                (!this.useMock && this.useAlternativeUrl != lastUrlWasAlt);

        if (configChanged) {
            clearDatabaseOnModeSwitch(invoiceDao);

            // Guardar nueva configuración
            prefs.edit()
                    .putBoolean(KEY_LAST_MODE_WAS_MOCK, this.useMock)
                    .putBoolean(KEY_LAST_URL_WAS_ALT, this.useAlternativeUrl)
                    .apply();
        }

        InvoiceRemoteDataSource remoteDataSource = new InvoiceRemoteDataSourceImpl(apiService);
        return new InvoiceRepositoryImpl(remoteDataSource, invoiceDao, this.useMock);
    }

    public InstallationRepository provideInstallationRepository() {
        return new InstallationRepositoryImpl(provideApiService());
    }

    private ApiService provideApiService() {
        ApiClientManager.getInstance().init(context);
        // Pasamos ambos flags
        return ApiClientManager.getInstance().getApiService(useMock, useAlternativeUrl, context);
    }

    private InvoiceDao provideInvoiceDao() {
        return AppDatabase.getInstance(context).invoiceDao();
    }

    private SharedPreferences provideSharedPrefs() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void clearDatabaseOnModeSwitch(InvoiceDao invoiceDao) {
        try {
            Executors.newSingleThreadExecutor().submit(invoiceDao::deleteAll).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
