package com.nexosolar.android.core;


/**
 * Clasificador de errores corregido.
 */
public class ErrorClassifier {

    public enum ErrorType {
        NONE,
        NETWORK,        // Sin internet, timeout, DNS fail
        SERVER,         // HTTP 4xx, 5xx
        UNKNOWN
    }

    /**
     * Clasifica una excepción analizando su tipo y origen.
     */
    public static ErrorType classify(Throwable error) {
        if (error == null) return ErrorType.UNKNOWN;

        // 1. Detectar si es un Timeout o un error HTTP (Ambos son culpa del servidor/infraestructura)
        String className = error.getClass().getName();

        if (className.contains("HttpException") ||
                error instanceof java.net.SocketTimeoutException ||
                className.contains("SocketTimeoutException")) {

            return ErrorType.SERVER;
        }

        // 2. Si es otra IOException (como UnknownHostException -> No hay internet)
        if (error instanceof java.io.IOException) {
            return ErrorType.NETWORK;
        }

        return ErrorType.UNKNOWN;
    }

    /**
     * Sobrecarga para cuando ya tenemos el código HTTP (desde el Repositorio/DataSource)
     */
    public static ErrorType classifyHttp(int code) {
        if (code >= 500) return ErrorType.SERVER; // Error interno del servidor
        if (code >= 400) return ErrorType.SERVER; // Error de cliente (Bad Request, etc)
        return ErrorType.UNKNOWN;
    }

    public static String getErrorMessage(ErrorType type, Throwable error) {
        switch (type) {
            case NETWORK:
                return "No hay conexión a internet. Revisa tu red.";
            case SERVER:
                return "El servidor no responde correctamente (Error 500/400).";
            case UNKNOWN:
                return "Error inesperado: " + (error != null ? error.getMessage() : "Desconocido");
            default:
                return "Ha ocurrido un error";
        }
    }
}