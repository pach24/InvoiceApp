package com.nexosolar.android.domain.repository;

/**
 * Interfaz genérica para comunicar resultados asíncronos desde la capa de Datos hacia Dominio.
 * Evita la dependencia de librerías de terceros (RxJava, Coroutines) en interfaces puras de Java.
 *
 * @param <T> Tipo de dato esperado en caso de éxito.
 */
public interface RepositoryCallback<T> {

    // ===== Métodos públicos =====

    void onSuccess(T data);

    void onError(Throwable error);
}
