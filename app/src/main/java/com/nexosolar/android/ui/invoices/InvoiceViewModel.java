package com.nexosolar.android.ui.invoices;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.nexosolar.android.core.ErrorClassifier;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;
import com.nexosolar.android.ui.invoices.managers.InvoiceDataManager;
import com.nexosolar.android.ui.invoices.managers.InvoiceFilterManager;
import com.nexosolar.android.ui.invoices.managers.InvoiceStateManager;
import com.nexosolar.android.ui.invoices.managers.InvoiceStatisticsCalculator;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel coordinador para la pantalla de listado de facturas.
 *
 * Responsabilidades:
 * - Coordinar la interacción entre managers especializados
 * - Exponer LiveData para observación desde la UI
 * - Gestionar el ciclo de vida de recursos (Handler, Executor)
 * - Actuar como facade para operaciones de alto nivel
 *
 * Sigue el patrón Facade para simplificar la interfaz hacia la UI.
 */
public class InvoiceViewModel extends ViewModel {

    // ===== Variables de instancia =====

    // Managers especializados (Single Responsibility Principle)
    private final InvoiceDataManager dataManager;
    private final InvoiceFilterManager filterManager;
    private final InvoiceStateManager stateManager;
    private final InvoiceStatisticsCalculator statisticsCalculator; // Declarado como final

    // Utilidades de threading
    private final ExecutorService backgroundExecutor;
    private final Handler mainThreadHandler;

    // Flags de control
    private boolean isFirstLoad = true;

    // ===== Constructores =====

    /**
     * Constructor principal con inyección de dependencias.
     *
     * @param getInvoicesUseCase Caso de uso para obtener facturas
     * @param filterInvoicesUseCase Caso de uso para filtrar facturas
     */
    public InvoiceViewModel(GetInvoicesUseCase getInvoicesUseCase,
                            FilterInvoicesUseCase filterInvoicesUseCase) {
        // Inicializar managers con sus responsabilidades específicas
        this.dataManager = new InvoiceDataManager(getInvoicesUseCase);
        this.filterManager = new InvoiceFilterManager(filterInvoicesUseCase);
        this.stateManager = new InvoiceStateManager();

        // CORRECCIÓN: Inicializar el statisticsCalculator
        this.statisticsCalculator = new InvoiceStatisticsCalculator();

        // Configurar threading
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
        this.mainThreadHandler = new Handler(Looper.getMainLooper());

        // Cargar datos iniciales
        cargarFacturas();
    }

    // ===== Getters de LiveData (Interfaz pública) =====

    /**
     * Obtiene la lista observable de facturas.
     * Esta lista refleja el estado actual (filtrado o completo).
     *
     * @return LiveData con la lista actual de facturas
     */
    public LiveData<List<Invoice>> getFacturas() {
        return dataManager.getInvoices();
    }

    /**
     * Obtiene el estado actual de la UI.
     *
     * @return LiveData con el estado (LOADING, ERROR, DATA, EMPTY)
     */
    public LiveData<InvoiceStateManager.ViewState> getViewState() {
        return stateManager.getCurrentState();
    }

    /**
     * Obtiene los filtros actualmente activos.
     *
     * @return LiveData con los filtros aplicados
     */
    public LiveData<InvoiceFilters> getFiltrosActuales() {
        return filterManager.getCurrentFilters();
    }

    /**
     * Obtiene mensajes de error de validación de filtros.
     *
     * @return LiveData con mensaje de error o null si es válido
     */
    public LiveData<String> getErrorValidacion() {
        return filterManager.getValidationError();
    }

    /**
     * Obtiene mensajes de error generales de la aplicación.
     *
     * @return LiveData con mensaje de error o null si no hay error
     */
    public LiveData<String> getErrorMessage() {
        return stateManager.getErrorMessage();
    }

    /**
     * Indica si se debe mostrar pantalla de error vacía.
     *
     * @return LiveData booleano con indicador de error
     */
    public LiveData<Boolean> getShowEmptyError() {
        return stateManager.getShowEmptyError();
    }

    // ===== Métodos públicos principales =====

    /**
     * Carga las facturas desde la fuente de datos.
     * Implementa estrategia inteligente: muestra caché inmediata si existe,
     * luego actualiza en segundo plano si es necesario.
     */
    public void cargarFacturas() {
        // Mostrar caché inmediata si existe (mejor UX)
        if (dataManager.hasCachedData() && !isFirstLoad) {
            stateManager.showData();
            return;
        }

        // Primera carga o sin caché: mostrar loading
        stateManager.showLoading();

        dataManager.loadInvoices(new InvoiceDataManager.LoadCallback() {
            @Override
            public void onSuccess(List<Invoice> invoices) {
                mainThreadHandler.post(() -> {
                    isFirstLoad = false;

                    if (invoices == null || invoices.isEmpty()) {
                        stateManager.showEmpty();
                    } else {
                        // Inicializar filtros con valores calculados de los datos
                        filterManager.resetFilters(invoices);
                        stateManager.showData();
                    }
                });
            }

            @Override
            public void onError(Throwable error) {
                mainThreadHandler.post(() -> {
                    isFirstLoad = false;
                    handleLoadError(error);
                });
            }
        });
    }

    /**
     * Aplica nuevos filtros a la lista de facturas.
     * La operación de filtrado se ejecuta en segundo plano.
     *
     * @param filters Filtros a aplicar
     */
    public void actualizarFiltros(InvoiceFilters filters) {
        Log.d("VIEWMODEL", "Iniciando actualizarFiltros");

        filterManager.updateFilters(filters);
        stateManager.showLoading();

        // Pequeño delay para que se muestre el shimmer antes de procesar
        mainThreadHandler.postDelayed(() -> {
            backgroundExecutor.execute(() -> {
                try {
                    Log.d("VIEWMODEL", "Procesando filtros en background");
                    Thread.sleep(300); // 0.3 segundo para ver claramente el shimmer
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Invoice> filtered = filterManager.applyCurrentFilters(
                        dataManager.getOriginalInvoices()
                );

                Log.d("VIEWMODEL", "Filtrado completado: " + filtered.size() + " elementos");

                mainThreadHandler.post(() -> {
                    dataManager.setInvoices(filtered);

                    if (filtered.isEmpty()) {
                        stateManager.showEmpty();
                        Log.d("VIEWMODEL", "Mostrando estado EMPTY");
                    } else {
                        stateManager.showData();
                        Log.d("VIEWMODEL", "Mostrando estado DATA");
                    }
                });
            });
        }, 300); // Pequeño delay de 300ms para asegurar que el loading se muestra
    }


    /**
     * Resetea todos los filtros a valores por defecto.
     * Restaura la lista completa de facturas.
     */
    public void resetearFiltros() {
        filterManager.resetFilters(dataManager.getOriginalInvoices());
        dataManager.setInvoices(dataManager.getOriginalInvoices());
        stateManager.showData();
    }

    /**
     * Actualiza el estado de los filtros sin aplicarlos inmediatamente.
     * Útil para sincronizar UI durante la edición de filtros.
     *
     * @param filters Nuevo estado de filtros
     */
    public void actualizarEstadoFiltros(InvoiceFilters filters) {
        filterManager.updateFilters(filters);
    }

    // ===== Métodos de utilidad (delegados a managers) =====

    /**
     * Calcula el importe máximo de las facturas originales.
     */
    public float getMaxImporte() {
        return statisticsCalculator.calculateMaxAmount(dataManager.getOriginalInvoices());
    }

    public LocalDate getOldestDate() {
        return statisticsCalculator.calculateOldestDate(dataManager.getOriginalInvoices());
    }

    public LocalDate getNewestDate() {
        return statisticsCalculator.calculateNewestDate(dataManager.getOriginalInvoices());
    }

    /**
     * Verifica si hay datos cargados en el ViewModel.
     *
     * @return true si hay al menos una factura cargada
     */
    public boolean hayDatosCargados() {
        return !dataManager.getOriginalInvoices().isEmpty();
    }

    /**
     * Verifica si hay filtros activos aplicados.
     *
     * @return true si hay al menos un filtro no por defecto activo
     */
    public boolean hayFiltrosActivos() {
        return filterManager.hasActiveFilters();
    }

    // ===== Métodos privados de ayuda =====

    /**
     * Maneja errores de carga de forma inteligente.
     * Distingue entre errores recuperables y no recuperables.
     *
     * @param error Excepción producida durante la carga
     */
    private void handleLoadError(Throwable error) {
        // 1. Clasificamos el error usando la lógica mejorada
        ErrorClassifier.ErrorType errorType = ErrorClassifier.classify(error);

        // Si tenemos datos en caché, no bloqueamos la pantalla con un error
        if (dataManager.hasCachedData()) {
            stateManager.showData();
            return;
        }

        // 2. Ejecutamos la transición de estado correspondiente
        String message = ErrorClassifier.getErrorMessage(errorType, error);

        switch (errorType) {
            case SERVER:
                stateManager.showServerError(message); // <-- Esto mostrará el layout de error de servidor
                break;

            case NETWORK:
                stateManager.showNetworkError(message); // <-- Esto mostrará el de conexión
                break;

            case UNKNOWN:
            default:
                stateManager.showServerError("Error inesperado: " + message);
                break;
        }
    }

    /**
     * Muestra error con delay para evitar parpadeos en UI.
     * Útil cuando la respuesta es muy rápida pero queremos mostrar feedback.
     *
     * @param errorType Tipo de error a mostrar
     * @param delayMs Milisegundos de delay antes de mostrar
     */
    // CORRECCIÓN EN InvoiceViewModel.java
    private void showErrorWithDelay(ErrorClassifier.ErrorType errorType, long delayMs) {
        mainThreadHandler.postDelayed(() -> {
            String message = ErrorClassifier.getErrorMessage(errorType, null);

            if (errorType == ErrorClassifier.ErrorType.SERVER) {
                stateManager.showServerError(message);
            } else {
                stateManager.showNetworkError(message);
            }
        }, delayMs);
    }

    // ===== Gestión del ciclo de vida =====

    @Override
    protected void onCleared() {
        super.onCleared();

        // Limpiar recursos para evitar memory leaks
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }

        if (mainThreadHandler != null) {
            mainThreadHandler.removeCallbacksAndMessages(null);
        }
    }
}