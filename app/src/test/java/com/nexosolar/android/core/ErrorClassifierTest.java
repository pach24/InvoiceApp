package com.nexosolar.android.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Tests unitarios para ErrorClassifier.
 * Valida la lógica de clasificación de errores según tipo de excepción.
 */
public class ErrorClassifierTest {

    // ========== Tests para classify(Throwable) ==========

    @Test
    public void classify_whenNull_returnsUnknown() {
        // GIVEN: Error nulo

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classify(null);

        // THEN: Retorna UNKNOWN
        assertEquals("Un error nulo debería clasificarse como UNKNOWN",
                ErrorClassifier.ErrorType.UNKNOWN, result);
    }

    @Test
    public void classify_whenSocketTimeout_returnsServer() {
        // GIVEN: SocketTimeoutException (servidor no respondió a tiempo)
        SocketTimeoutException error = new SocketTimeoutException("Connection timed out");

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classify(error);

        // THEN: Retorna SERVER (timeout es responsabilidad del servidor)
        assertEquals("Un timeout debería clasificarse como error de SERVER",
                ErrorClassifier.ErrorType.SERVER, result);
    }

    @Test
    public void classify_whenUnknownHost_returnsNetwork() {
        // GIVEN: UnknownHostException (sin internet o fallo de DNS)
        UnknownHostException error = new UnknownHostException("Unable to resolve host");

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classify(error);

        // THEN: Retorna NETWORK
        assertEquals("UnknownHostException debería clasificarse como error de NETWORK",
                ErrorClassifier.ErrorType.NETWORK, result);
    }

    @Test
    public void classify_whenGenericIOException_returnsNetwork() {
        // GIVEN: IOException genérica (fallo de conexión)
        IOException error = new IOException("Network is unreachable");

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classify(error);

        // THEN: Retorna NETWORK
        assertEquals("IOException genérica debería clasificarse como error de NETWORK",
                ErrorClassifier.ErrorType.NETWORK, result);
    }

    @Test
    public void classify_whenRuntimeException_returnsUnknown() {
        // GIVEN: Excepción no relacionada con red
        RuntimeException error = new RuntimeException("Unexpected error");

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classify(error);

        // THEN: Retorna UNKNOWN
        assertEquals("RuntimeException no relacionada con red debería ser UNKNOWN",
                ErrorClassifier.ErrorType.UNKNOWN, result);
    }

    // ========== Tests para classifyHttp(int) ==========

    @Test
    public void classifyHttp_when500_returnsServer() {
        // GIVEN: Código HTTP 500 Internal Server Error

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classifyHttp(500);

        // THEN: Retorna SERVER
        assertEquals("Código HTTP 500 debería clasificarse como error de SERVER",
                ErrorClassifier.ErrorType.SERVER, result);
    }

    @Test
    public void classifyHttp_when503_returnsServer() {
        // GIVEN: Código HTTP 503 Service Unavailable

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classifyHttp(503);

        // THEN: Retorna SERVER
        assertEquals("Código HTTP 503 debería clasificarse como error de SERVER",
                ErrorClassifier.ErrorType.SERVER, result);
    }

    @Test
    public void classifyHttp_when404_returnsServer() {
        // GIVEN: Código HTTP 404 Not Found (error de cliente)

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classifyHttp(404);

        // THEN: Retorna SERVER (tratado como problema del servidor)
        assertEquals("Código HTTP 404 debería clasificarse como error de SERVER",
                ErrorClassifier.ErrorType.SERVER, result);
    }

    @Test
    public void classifyHttp_when401_returnsServer() {
        // GIVEN: Código HTTP 401 Unauthorized

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classifyHttp(401);

        // THEN: Retorna SERVER
        assertEquals("Código HTTP 401 debería clasificarse como error de SERVER",
                ErrorClassifier.ErrorType.SERVER, result);
    }

    @Test
    public void classifyHttp_when200_returnsUnknown() {
        // GIVEN: Código HTTP 200 OK (código de éxito)

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classifyHttp(200);

        // THEN: Retorna UNKNOWN (no es un error)
        assertEquals("Código HTTP 200 no es un error, debería ser UNKNOWN",
                ErrorClassifier.ErrorType.UNKNOWN, result);
    }

    @Test
    public void classifyHttp_when300_returnsUnknown() {
        // GIVEN: Código HTTP 301 Redirect

        // WHEN: Clasificamos
        ErrorClassifier.ErrorType result = ErrorClassifier.classifyHttp(301);

        // THEN: Retorna UNKNOWN
        assertEquals("Código HTTP 301 debería clasificarse como UNKNOWN",
                ErrorClassifier.ErrorType.UNKNOWN, result);
    }

    // ========== Tests para getErrorMessage() ==========

    @Test
    public void getErrorMessage_forNetwork_returnsCorrectMessage() {
        // GIVEN: Tipo de error NETWORK

        // WHEN: Obtenemos mensaje
        String message = ErrorClassifier.getErrorMessage(
                ErrorClassifier.ErrorType.NETWORK,
                null
        );

        // THEN: Mensaje menciona conexión/internet
        assertEquals("El mensaje para NETWORK debería mencionar la conexión",
                "No hay conexión a internet. Revisa tu red.", message);
    }

    @Test
    public void getErrorMessage_forServer_returnsCorrectMessage() {
        // GIVEN: Tipo de error SERVER

        // WHEN: Obtenemos mensaje
        String message = ErrorClassifier.getErrorMessage(
                ErrorClassifier.ErrorType.SERVER,
                null
        );

        // THEN: Mensaje menciona servidor
        assertEquals("El mensaje para SERVER debería mencionar el servidor",
                "El servidor no responde correctamente (Error 500/400).", message);
    }

    @Test
    public void getErrorMessage_forUnknown_includesErrorMessage() {
        // GIVEN: Error UNKNOWN con mensaje personalizado
        Exception error = new Exception("Custom error details");

        // WHEN: Obtenemos mensaje
        String message = ErrorClassifier.getErrorMessage(
                ErrorClassifier.ErrorType.UNKNOWN,
                error
        );

        // THEN: Incluye el mensaje de la excepción
        assertEquals("El mensaje debería incluir los detalles de la excepción",
                "Error inesperado: Custom error details", message);
    }

    @Test
    public void getErrorMessage_forUnknownWithNull_handlesGracefully() {
        // GIVEN: Error UNKNOWN con throwable nulo

        // WHEN: Obtenemos mensaje
        String message = ErrorClassifier.getErrorMessage(
                ErrorClassifier.ErrorType.UNKNOWN,
                null
        );

        // THEN: Mensaje por defecto
        assertEquals("Debería manejar error nulo con mensaje por defecto",
                "Error inesperado: Desconocido", message);
    }
}
