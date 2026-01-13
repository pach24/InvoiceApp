package com.nexosolar.android.ui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.nexosolar.android.domain.GetInvoicesUseCase;
import com.nexosolar.android.domain.Invoice;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceViewModelTest {

    // Regla para que LiveData funcione de forma síncrona en tests
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private GetInvoicesUseCase useCase;

    private InvoiceViewModel viewModel;
    private MutableLiveData<List<Invoice>> facturasLiveData;

    @Before
    public void setUp() {
        // 1. Preparamos el mock del LiveData
        facturasLiveData = new MutableLiveData<>();
        when(useCase.invoke()).thenReturn(facturasLiveData);

        // 2. Instanciamos el ViewModel con el UseCase simulado
        viewModel = new InvoiceViewModel(useCase);
    }

    @Test
    public void alIniciar_LlamaARefresh() {
        // Verificamos que el constructor pida datos nuevos automáticamente
        verify(useCase).refresh();
    }

    @Test
    public void filtrarFacturas_PorImporte_DevuelveCorrectas() {

        // 1. GIVEN: Creamos una lista con 3 facturas de prueba
        List<Invoice> datosPrueba = new ArrayList<>();
        // Hacemos el cast (float)
        datosPrueba.add(crearFactura(100.0f, "Pagada"));    // > 80
        datosPrueba.add(crearFactura(500.0f, "Pendiente")); // > 80
        datosPrueba.add(crearFactura(50.0f, "Pagada"));     // < 80

        // Inyectamos estos datos en el ViewModel
        viewModel.setFacturasOriginalesTest(datosPrueba);

        // 2. WHEN: Filtramos facturas con importe mínimo de 80.0
        List<Invoice> resultado = viewModel.filtrarFacturas(
                null,
                null,
                null,
                80.0,
                null
        );

        // 3. THEN: Deberían quedar exactamente 2 facturas
        assertEquals(2, resultado.size());
    }

    // Método helper
    private Invoice crearFactura(float importe, String estado) {
        Invoice invoice = new Invoice();
        // Aquí pasamos el float directamente
        invoice.setImporteOrdenacion(importe);
        invoice.setDescEstado(estado);
        invoice.setFecha(LocalDate.now());
        return invoice;
    }
}


