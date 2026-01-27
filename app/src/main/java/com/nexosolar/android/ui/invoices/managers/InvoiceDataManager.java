package com.nexosolar.android.ui.invoices.managers;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor especializado en datos de facturas.
 *
 * Responsabilidades:
 * - Gestionar el ciclo de vida de los datos (carga, caché, actualización)
 * - Mantener copia original de datos para operaciones de filtrado
 * - Coordinar entre fuente remota y caché local
 * - Notificar cambios a través de LiveData reactivo
 *
 * Sigue el patrón Repository para abstraer el origen de datos.
 */
public class InvoiceDataManager {

    // ===== Interfaces internas =====

    /**
     * Callback para operaciones asíncronas de carga de datos.
     */
    public interface LoadCallback {
        void onSuccess(List<Invoice> invoices);
        void onError(Throwable error);
    }

    // ===== Variables de instancia =====

    private final GetInvoicesUseCase getInvoicesUseCase;
    private List<Invoice> originalInvoices = new ArrayList<>();
    private final MutableLiveData<List<Invoice>> _invoices = new MutableLiveData<>();

    // ===== Constructores =====

    public InvoiceDataManager(GetInvoicesUseCase getInvoicesUseCase) {
        this.getInvoicesUseCase = getInvoicesUseCase;
    }

    // ===== Getters de LiveData =====

    /**
     * Obtiene la lista observable de facturas.
     * Esta lista puede estar filtrada según criterios aplicados.
     *
     * @return LiveData con la lista actual de facturas
     */
    public LiveData<List<Invoice>> getInvoices() {
        return _invoices;
    }

    /**
     * Obtiene la lista original completa de facturas (sin filtros).
     * Útil para operaciones de filtrado y cálculos estadísticos.
     *
     * @return Lista inmutable de facturas originales
     */
    public List<Invoice> getOriginalInvoices() {
        return new ArrayList<>(originalInvoices);
    }

    // ===== Métodos públicos de operación =====

    /**
     * Carga facturas desde la fuente de datos.
     * Implementa estrategia de caché: primero caché local, luego red si es necesario.
     *
     * @param callback Callback para notificar resultado de la operación
     */
    public void loadInvoices(LoadCallback callback) {
        // Primero verificar si ya tenemos datos en memoria
        boolean hasCachedData = !originalInvoices.isEmpty();

        if (hasCachedData) {
            // Notificar inmediatamente con datos cacheados
            callback.onSuccess(new ArrayList<>(originalInvoices));
            return;
        }

        // Si no hay caché, cargar desde use case
        getInvoicesUseCase.invoke(new RepositoryCallback<List<Invoice>>() {
            @Override
            public void onSuccess(List<Invoice> result) {
                // Guardar copia original para operaciones posteriores
                originalInvoices = new ArrayList<>(result);

                // Notificar a observadores internos
                _invoices.postValue(result);

                // Notificar al callback externo
                if (callback != null) {
                    callback.onSuccess(result);
                }
            }

            @Override
            public void onError(Throwable error) {
                // Notificar error al callback
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    /**
     * Establece una nueva lista de facturas (generalmente después de filtrado).
     *
     * @param invoices Nueva lista de facturas a mostrar
     */
    public void setInvoices(List<Invoice> invoices) {
        _invoices.postValue(invoices != null ? new ArrayList<>(invoices) : new ArrayList<>());
    }

    /**
     * Actualiza las facturas originales (después de recarga forzada).
     * Útil cuando se sabe que los datos han cambiado en servidor.
     *
     * @param invoices Nueva lista de facturas originales
     */
    public void updateOriginalInvoices(List<Invoice> invoices) {
        this.originalInvoices = new ArrayList<>(invoices);
    }

    /**
     * Fuerza una recarga desde la fuente remota.
     * Ignora caché local y solicita datos frescos.
     *
     * @param callback Callback para notificar resultado
     */
    public void refreshInvoices(LoadCallback callback) {
        getInvoicesUseCase.refresh(new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean success) {
                // Después de refrescar, cargar datos actualizados
                loadInvoices(callback);
            }

            @Override
            public void onError(Throwable error) {
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    // ===== Métodos de consulta =====

    /**
     * Verifica si hay datos cacheados disponibles.
     *
     * @return true si hay al menos una factura en caché, false en caso contrario
     */
    public boolean hasCachedData() {
        return !originalInvoices.isEmpty();
    }

    /**
     * Obtiene el número total de facturas originales.
     *
     * @return Cantidad de facturas en la lista original
     */
    public int getOriginalCount() {
        return originalInvoices.size();
    }


    public void invalidateCache() {
        // Usamos postValue para asegurar que la limpieza se notifique al hilo principal
        _invoices.postValue(new ArrayList<>());
    }

    /**
     * Limpia todos los datos almacenados.
     * Útil al cambiar de modo (mock/real) o al cerrar sesión.
     */
    public void clearAllData() {
        originalInvoices.clear();
        _invoices.postValue(new ArrayList<>());
    }
}