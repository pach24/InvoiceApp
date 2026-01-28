package com.nexosolar.android.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.repository.InvoiceRepository;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests unitarios para GetInvoicesUseCase.
 * Valida la interacción con el repositorio y el manejo de callbacks.
 */
@RunWith(MockitoJUnitRunner.class)
public class GetInvoicesUseCaseTest {

    @Mock
    private InvoiceRepository mockRepository;

    private GetInvoicesUseCase useCase;

    @Before
    public void setUp() {
        useCase = new GetInvoicesUseCase(mockRepository);
    }

    @Test
    public void invoke_whenCalled_delegatesToRepository() {
        // GIVEN: Un callback mock para recibir resultados
        RepositoryCallback<List<Invoice>> mockCallback = createMockCallback();

        // WHEN: Invocamos el use case
        useCase.invoke(mockCallback);

        // THEN: Verifica que el repositorio fue llamado exactamente una vez
        verify(mockRepository, times(1)).getInvoices(any());
    }

    @Test
    public void invoke_whenRepositoryReturnsData_callsSuccessCallback() {
        // GIVEN: Repositorio configurado para retornar datos exitosamente
        List<Invoice> mockInvoices = createMockInvoices();
        configureRepositorySuccess(mockInvoices);

        // WHEN: Invocamos el use case
        RepositoryCallback<List<Invoice>> callback = createTestCallback(
                invoices -> {
                    // THEN: Verifica que se reciben los datos correctos
                    assertNotNull("Las facturas no deberían ser null", invoices);
                    assertEquals("Debería retornar 2 facturas", 2, invoices.size());
                    assertEquals("Primera factura debería tener importe 100",
                            100f, invoices.get(0).getInvoiceAmount(), 0.01);
                },
                error -> {
                    throw new AssertionError("No debería llamarse onError en caso de éxito");
                }
        );

        useCase.invoke(callback);
    }

    @Test
    public void invoke_whenRepositoryFails_callsErrorCallback() {
        // GIVEN: Repositorio configurado para retornar error
        Throwable mockError = new Exception("Error de red");
        configureRepositoryError(mockError);

        // WHEN: Invocamos el use case
        RepositoryCallback<List<Invoice>> callback = createTestCallback(
                invoices -> {
                    throw new AssertionError("No debería llamarse onSuccess en caso de error");
                },
                error -> {
                    // THEN: Verifica que se recibe el error
                    assertNotNull("El error no debería ser null", error);
                    assertEquals("El mensaje de error debería coincidir",
                            "Error de red", error.getMessage());
                }
        );

        useCase.invoke(callback);
    }

    @Test
    public void refresh_whenCalled_delegatesToRepositoryRefresh() {
        // GIVEN: Un callback mock para recibir confirmación
        RepositoryCallback<Boolean> mockCallback = createMockCallback();

        // WHEN: Llamamos a refresh
        useCase.refresh(mockCallback);

        // THEN: Verifica que se llamó a refreshInvoices exactamente una vez
        verify(mockRepository, times(1)).refreshInvoices(any());
    }

    @Test
    public void refresh_whenRepositoryRefreshSucceeds_callsSuccessCallback() {
        // GIVEN: Repositorio configurado para refrescar exitosamente
        configureRepositoryRefreshSuccess();

        // WHEN: Llamamos a refresh
        RepositoryCallback<Boolean> callback = createTestCallback(
                success -> {
                    // THEN: Verifica que se recibe confirmación de éxito
                    assertNotNull("El resultado no debería ser null", success);
                    assertEquals("El refresh debería retornar true", Boolean.TRUE, success);
                },
                error -> {
                    throw new AssertionError("No debería llamarse onError en caso de éxito");
                }
        );

        useCase.refresh(callback);
    }

    // ========== Métodos auxiliares ==========

    /**
     * Crea una lista de facturas mock para pruebas.
     */
    private List<Invoice> createMockInvoices() {
        List<Invoice> invoices = new ArrayList<>();

        Invoice invoice1 = new Invoice();
        invoice1.setInvoiceAmount(100f);
        invoice1.setInvoiceStatus("Pagada");
        invoice1.setInvoiceDate(LocalDate.of(2025, 1, 1));

        Invoice invoice2 = new Invoice();
        invoice2.setInvoiceAmount(200f);
        invoice2.setInvoiceStatus("Pendiente de pago");
        invoice2.setInvoiceDate(LocalDate.of(2025, 2, 1));

        invoices.add(invoice1);
        invoices.add(invoice2);

        return invoices;
    }

    /**
     * Configura el mock del repositorio para retornar datos exitosamente.
     */
    private void configureRepositorySuccess(List<Invoice> invoices) {
        doAnswer(invocation -> {
            RepositoryCallback<List<Invoice>> callback = invocation.getArgument(0);
            callback.onSuccess(invoices);
            return null;
        }).when(mockRepository).getInvoices(any());
    }

    /**
     * Configura el mock del repositorio para retornar error.
     */
    private void configureRepositoryError(Throwable error) {
        doAnswer(invocation -> {
            RepositoryCallback<List<Invoice>> callback = invocation.getArgument(0);
            callback.onError(error);
            return null;
        }).when(mockRepository).getInvoices(any());
    }

    /**
     * Configura el mock del repositorio para refresh exitoso.
     */
    private void configureRepositoryRefreshSuccess() {
        doAnswer(invocation -> {
            RepositoryCallback<Boolean> callback = invocation.getArgument(0);
            if (callback != null) {
                callback.onSuccess(true);
            }
            return null;
        }).when(mockRepository).refreshInvoices(any());
    }

    /**
     * Crea un callback de prueba con lambdas.
     */
    private <T> RepositoryCallback<T> createTestCallback(
            SuccessHandler<T> onSuccess,
            ErrorHandler onError) {
        return new RepositoryCallback<T>() {
            @Override
            public void onSuccess(T data) {
                onSuccess.handle(data);
            }

            @Override
            public void onError(Throwable error) {
                onError.handle(error);
            }
        };
    }

    /**
     * Crea un callback mock simple (para verificaciones básicas).
     */
    @SuppressWarnings("unchecked")
    private <T> RepositoryCallback<T> createMockCallback() {
        return (RepositoryCallback<T>) new RepositoryCallback<Object>() {
            @Override
            public void onSuccess(Object data) {}

            @Override
            public void onError(Throwable error) {}
        };
    }

    // Interfaces auxiliares para lambdas
    private interface SuccessHandler<T> {
        void handle(T data);
    }

    private interface ErrorHandler {
        void handle(Throwable error);
    }
}
