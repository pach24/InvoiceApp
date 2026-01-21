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

/**
 * Módulo de inyección de dependencias manual para la capa de datos.
 *
 * Actúa como factory centralizado para proporcionar instancias configuradas
 * de repositorios, fuentes de datos y DAOs. Implementa el patrón Service Locator
 * simplificado sin usar frameworks como Dagger/Hilt.
 *
 * Gestiona dos modos de operación:
 * - Modo Real: API de producción + caché local inteligente
 * - Modo Mock: API simulada + recarga forzada para testing
 *
 * Cuando cambia el modo (Real ↔ Mock), limpia la base de datos para evitar
 * inconsistencias entre datos reales y simulados.
 */
public class DataModule {

    // ===== Constantes =====

    private static final String PREFS_NAME = "RepoPrefs";
    private static final String KEY_LAST_MODE_WAS_MOCK = "last_mode_was_mock";

    // ===== Variables de instancia =====

    private final Context context;
    private final boolean useMock;

    // ===== Constructores =====

    /**
     * Crea el módulo de datos con el modo especificado.
     *
     * @param context Contexto de la aplicación
     * @param useMock true para usar API mock, false para API real
     */
    public DataModule(Context context, boolean useMock) {
        this.context = context.getApplicationContext();
        this.useMock = useMock;
    }

    // ===== Métodos públicos (Providers) =====

    /**
     * Proporciona la implementación del repositorio de facturas.
     *
     * Configura el repositorio según el modo activo:
     * - Mock: alwaysReload=true → siempre consulta la API para testing circular
     * - Real: alwaysReload=false → usa caché local cuando está disponible
     *
     * Limpia la base de datos si detecta un cambio de modo para evitar mezclar
     * datos reales con datos simulados.
     *
     * @return Instancia configurada de InvoiceRepository
     */
    public InvoiceRepository provideInvoiceRepository() {
        ApiService apiService = provideApiService();
        InvoiceDao invoiceDao = provideInvoiceDao();
        SharedPreferences prefs = provideSharedPrefs();

        // Detecta cambio de modo (Real ↔ Mock) y limpia la BD si es necesario
        boolean lastModeWasMock = prefs.getBoolean(KEY_LAST_MODE_WAS_MOCK, false);
        if (this.useMock != lastModeWasMock) {
            clearDatabaseOnModeSwitch(invoiceDao);
            prefs.edit().putBoolean(KEY_LAST_MODE_WAS_MOCK, this.useMock).apply();
        }

        InvoiceRemoteDataSource remoteDataSource = new InvoiceRemoteDataSourceImpl(apiService);

        // Configura estrategia de caché: Mock siempre recarga, Real usa caché
        return new InvoiceRepositoryImpl(remoteDataSource, invoiceDao, this.useMock);
    }

    /**
     * Proporciona la implementación del repositorio de instalaciones.
     *
     * No requiere caché local ya que los datos de instalación se consultan
     * bajo demanda y no necesitan persistencia.
     *
     * @return Instancia de InstallationRepository
     */
    public InstallationRepository provideInstallationRepository() {
        return new InstallationRepositoryImpl(provideApiService());
    }

    // ===== Métodos privados (Providers internos) =====

    /**
     * Proporciona la instancia del servicio API (Retrofit).
     *
     * Inicializa ApiClientManager si no lo está y devuelve la instancia
     * correspondiente al modo activo (Mock o Real).
     *
     * @return Instancia de ApiService configurada
     */
    private ApiService provideApiService() {
        ApiClientManager.getInstance().init(context);
        return ApiClientManager.getInstance().getApiService(useMock, context);
    }

    /**
     * Proporciona el DAO de facturas desde la base de datos singleton.
     *
     * @return Instancia del DAO de Room
     */
    private InvoiceDao provideInvoiceDao() {
        return AppDatabase.getInstance(context).invoiceDao();
    }

    /**
     * Proporciona SharedPreferences para almacenar el estado del modo activo.
     *
     * @return Instancia de SharedPreferences privadas de la app
     */
    private SharedPreferences provideSharedPrefs() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Limpia la base de datos cuando se detecta un cambio de modo.
     *
     * Ejecuta deleteAll de forma síncrona usando un ExecutorService temporal
     * para garantizar que la limpieza se complete antes de continuar.
     *
     * @param invoiceDao DAO para ejecutar la operación de limpieza
     */
    private void clearDatabaseOnModeSwitch(InvoiceDao invoiceDao) {
        try {
            Executors.newSingleThreadExecutor().submit(() -> {
                invoiceDao.deleteAll();
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
