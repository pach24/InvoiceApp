package com.nexosolar.android.domain;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import com.nexosolar.android.data.InvoiceRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetInvoicesUseCaseTest {

    // 1. Simulamos el Repositorio (no usamos el real)
    @Mock
    private InvoiceRepository repository;

    // Clase a testear
    private GetInvoicesUseCase useCase;

    @Before
    public void setUp() {
        // Inicializamos el UseCase con el Mock del repositorio
        useCase = new GetInvoicesUseCase(repository);
    }

    @Test
    public void invoke_CallsRepositoryGetFacturas() {
        // When: Llamamos al método del UseCase
        useCase.invoke();

        // Then: Verificamos que el UseCase llamó al método getFacturas() del repo
        verify(repository, times(1)).getFacturas();
    }

    @Test
    public void refresh_CallsRepositoryRefreshFacturas() {
        // When: Llamamos a refresh
        useCase.refresh();

        // Then: Verificamos que el UseCase llamó al método refreshFacturas() del repo
        verify(repository, times(1)).refreshFacturas();
    }
}
