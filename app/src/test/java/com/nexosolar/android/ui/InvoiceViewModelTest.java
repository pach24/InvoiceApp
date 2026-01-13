package com.nexosolar.android.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.nexosolar.android.domain.GetInvoicesUseCase;
import com.nexosolar.android.domain.Invoice;
import com.nexosolar.android.domain.RepositoryCallback;
import com.nexosolar.android.ui.invoices.InvoiceViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private GetInvoicesUseCase useCase;

    private InvoiceViewModel viewModel;

    @Before
    public void setUp() {
        // --- SIMULACIÓN DEL COMPORTAMIENTO ASÍNCRONO ---

        // Cuando el ViewModel llame a useCase.invoke(callback)...
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                // 1. Recuperamos el callback que le pasó el ViewModel
                RepositoryCallback<List<Invoice>> callback = invocation.getArgument(0);

                // 2. Simulamos que la base de datos devuelve una lista vacía inmediatamente
                callback.onSuccess(new ArrayList<>());
                return null;
            }
        }).when(useCase).invoke(any()); // interceptamos cualquier llamada a invoke

        // Inicializamos el ViewModel (esto dispara el 'invoke' simulado arriba)
        viewModel = new InvoiceViewModel(useCase);
    }

    @Test
    public void alIniciar_CargaFacturasLocalmente() {
        // Verificamos que al crearse, el ViewModel pidió datos al caso de uso
        verify(useCase).invoke(any());
    }

    @Test
    public void filtrarFacturas_PorImporte_DevuelveCorrectas() {
        // 1. GIVEN: Datos de prueba
        List<Invoice> datosPrueba = new ArrayList<>();
        datosPrueba.add(crearFactura(100.0f, "Pagada"));
        datosPrueba.add(crearFactura(500.0f, "Pendiente"));
        datosPrueba.add(crearFactura(50.0f, "Pagada"));

        // Inyectamos datos manualmente
        viewModel.setFacturasOriginalesTest(datosPrueba);

        // 2. WHEN: Filtramos > 80
        // Nota: Ahora filtrarFacturas actualiza el LiveData, no devuelve lista directa
        // Dependiendo de tu implementación de filtrarFacturas, si devuelve void, tenemos que observar el LiveData.


        viewModel.filtrarFacturas(null, null, null, 80.0, null);

        // 3. THEN: Observamos el valor actual del LiveData
        List<Invoice> resultado = viewModel.getFacturas().getValue();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    private Invoice crearFactura(float importe, String estado) {
        Invoice invoice = new Invoice();
        invoice.setImporteOrdenacion(importe);
        invoice.setDescEstado(estado);
        invoice.setFecha(LocalDate.now());
        return invoice;
    }
}
