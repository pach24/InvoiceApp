package com.nexosolar.android.domain.repository;

// Interfaz pura de Java para devolver resultados asíncronos
public interface RepositoryCallback<T> {
    void onSuccess(T datos);
    void onError(Throwable error);
}
