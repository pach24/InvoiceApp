package com.nexosolar.android.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetInvoicesUseCaseTest {

    @Mock
    private InvoiceRepository repository;

    private GetInvoicesUseCase useCase;

    @Before
    public void setUp() {
        useCase = new GetInvoicesUseCase(repository);
    }

    @Test
    public void invoke_CallsRepositoryGetFacturas() {
        // When: Llamamos al useCase pasando null o un mock (simulando que la UI pide datos)
        // No necesitamos un callback real para verificar que el método se llama
        useCase.invoke(null);

        // Then: Verificamos que el repositorio recibe la llamada pasando el argumento (null en este caso)
        verify(repository, times(1)).getFacturas(any());
    }

    @Test
    public void refresh_CallsRepositoryRefreshFacturas() {
        // When
        useCase.refresh(null);

        // Then
        verify(repository, times(1)).refreshFacturas(any());
    }
}
