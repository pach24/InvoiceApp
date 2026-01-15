// com.nexosolar.android.data.di.DataModule
package com.nexosolar.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.nexosolar.android.data.local.AppDatabase;
import com.nexosolar.android.data.local.InvoiceDao;
import com.nexosolar.android.data.remote.ApiClientManager;
import com.nexosolar.android.data.remote.ApiService;
import com.nexosolar.android.data.repository.InstallationRepositoryImpl;
import com.nexosolar.android.data.repository.InvoiceRepositoryImpl;
import com.nexosolar.android.domain.repository.InstallationRepository;
import com.nexosolar.android.domain.repository.InvoiceRepository;

/**
 * Módulo de Inyección Manual para la capa Data.
 * Encapsula toda la creación de dependencias "sucias" (Room, Retrofit, Prefs).
 */
public class DataModule {

    private final Context context;
    private final boolean useMock;

    // Singletons cacheados
    private ApiService apiService;
    private InvoiceDao invoiceDao;
    private SharedPreferences sharedPrefs;

    public DataModule(Context context, boolean useMock) {
        this.context = context.getApplicationContext();
        this.useMock = useMock;
    }

    // --- PROVEEDORES PÚBLICOS (Lo único que ve el módulo APP) ---

    public InvoiceRepository provideInvoiceRepository() {
        return new InvoiceRepositoryImpl(
                provideApiService(),
                provideInvoiceDao(),
                provideSharedPrefs(),
                useMock
        );
    }

    public InstallationRepository provideInstallationRepository() {
        return new InstallationRepositoryImpl(provideApiService());
    }

    // --- PROVEEDORES PRIVADOS (Detalles internos de Data) ---

    private ApiService provideApiService() {
        if (apiService == null) {
            ApiClientManager.getInstance().init(context);
            apiService = ApiClientManager.getInstance().getApiService(useMock, context);
        }
        return apiService;
    }

    private InvoiceDao provideInvoiceDao() {
        if (invoiceDao == null) {
            AppDatabase db = AppDatabase.getInstance(context);
            invoiceDao = db.invoiceDao();
        }
        return invoiceDao;
    }

    private SharedPreferences provideSharedPrefs() {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences("RepoPrefs", Context.MODE_PRIVATE);
        }
        return sharedPrefs;
    }
}
