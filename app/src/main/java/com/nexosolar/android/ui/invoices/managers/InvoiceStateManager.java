package com.nexosolar.android.ui.invoices.managers;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;

/**
 * Gestor especializado en estados de UI para la pantalla de facturas.
 *
 * Responsabilidades:
 * - Gestionar el ciclo de vida de los estados visuales (carga, error, datos, vacío)
 * - Mantener consistencia entre múltiples indicadores de estado
 * - Proporcionar métodos seguros para transiciones de estado
 *
 * Implementa el patrón State para encapsular la lógica de transiciones de UI.
 */
public class InvoiceStateManager {

    /**
     * Estados posibles de la UI de facturas.
     * Sigue una jerarquía clara: LOADING → (ERROR | DATA | EMPTY)
     */
    public enum ViewState {
        LOADING,           // Mostrando shimmer/indicador de carga
        ERROR_NETWORK,     // Error de conexión (wifi/red)
        ERROR_SERVER,      // Error del servidor (HTTP 4xx/5xx)
        EMPTY,             // Datos cargados pero lista vacía
        DATA               // Datos cargados y listos para mostrar
    }

    // ===== Variables de instancia =====

    private final MutableLiveData<ViewState> _currentState = new MutableLiveData<>(ViewState.LOADING);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _showEmptyError = new MutableLiveData<>(false);

    // ===== Getters de LiveData =====

    /**
     * Obtiene el estado actual de la UI como LiveData.
     *
     * @return LiveData con el estado actual (LOADING, ERROR_NETWORK, etc.)
     */
    public LiveData<ViewState> getCurrentState() {
        return _currentState;
    }

    /**
     * Obtiene el mensaje de error actual (si aplica).
     *
     * @return LiveData con mensaje de error o null si no hay error
     */
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    /**
     * Indica si se debe mostrar la pantalla de error vacía.
     * Diferencia entre "sin datos" (EMPTY) y "error que requiere acción" (showEmptyError=true).
     *
     * @return LiveData booleano indicando visibilidad de error
     */
    public LiveData<Boolean> getShowEmptyError() {
        return _showEmptyError;
    }

    // ===== Métodos de transición de estado =====

    /**
     * Activa el estado de carga (shimmer/loader visible).
     * Resetea cualquier error previo.
     */
    public void showLoading() {
        _currentState.setValue(ViewState.LOADING);
        _errorMessage.setValue(null);
        _showEmptyError.setValue(false);
    }

    /**
     * Muestra error de red con mensaje específico.
     *
     * @param message Mensaje descriptivo del error (opcional, puede ser null)
     */
    public void showNetworkError(String message) {
        _currentState.setValue(ViewState.ERROR_NETWORK);
        _errorMessage.setValue(message);
        _showEmptyError.setValue(true);
    }

    /**
     * Muestra error de servidor con mensaje específico.
     *
     * @param message Mensaje descriptivo del error (opcional, puede ser null)
     */
    public void showServerError(String message) {
        _currentState.setValue(ViewState.ERROR_SERVER);
        _errorMessage.setValue(message);
        _showEmptyError.setValue(true);
    }

    /**
     * Muestra estado con datos (lista visible).
     * Útil cuando la carga es exitosa y hay elementos para mostrar.
     */
    public void showData() {
        _currentState.setValue(ViewState.DATA);
        _errorMessage.setValue(null);
        _showEmptyError.setValue(false);
    }

    /**
     * Muestra estado vacío (sin datos pero sin error).
     * Diferente de ERROR: aquí la operación fue exitosa pero no hay elementos.
     */
    public void showEmpty() {
        _currentState.setValue(ViewState.EMPTY);
        _errorMessage.setValue(null);
        _showEmptyError.setValue(false);
    }

    /**
     * Restablece todos los estados a valores iniciales.
     * Útil al recargar datos o cambiar configuración.
     */
    public void reset() {
        _currentState.setValue(ViewState.LOADING);
        _errorMessage.setValue(null);
        _showEmptyError.setValue(false);
    }

    // ===== Métodos de consulta =====

    /**
     * Verifica si el estado actual es de carga.
     *
     * @return true si está en estado LOADING, false en caso contrario
     */
    public boolean isLoading() {
        return _currentState.getValue() == ViewState.LOADING;
    }

    /**
     * Verifica si el estado actual es de error.
     *
     * @return true si está en estado ERROR_NETWORK o ERROR_SERVER
     */
    public boolean isError() {
        ViewState state = _currentState.getValue();
        return state == ViewState.ERROR_NETWORK || state == ViewState.ERROR_SERVER;
    }

    /**
     * Verifica si el estado actual muestra datos.
     *
     * @return true si está en estado DATA, false en caso contrario
     */
    public boolean hasData() {
        return _currentState.getValue() == ViewState.DATA;
    }




}