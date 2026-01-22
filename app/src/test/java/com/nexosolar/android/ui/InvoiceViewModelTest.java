package com.nexosolar.android.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.repository.RepositoryCallback;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;
import com.nexosolar.android.domain.usecase.invoice.GetInvoicesUseCase;
import com.nexosolar.android.ui.invoices.InvoiceViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class InvoiceViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private GetInvoicesUseCase getInvoicesUseCase;

    // Usamos la implementación REAL del filtro, no un mock, para testear la integración ViewModel-Filtro
    private FilterInvoicesUseCase filterInvoicesUseCase;

    private InvoiceViewModel viewModel;

    @Before
    public void setUp() {
        filterInvoicesUseCase = new FilterInvoicesUseCase();

        // Simulamos respuesta vacía al cargar facturas iniciales
        doAnswer(invocation -> {
            RepositoryCallback<List<Invoice>> callback = invocation.getArgument(0);
            callback.onSuccess(new ArrayList<>());
            return null;
        }).when(getInvoicesUseCase).invoke(any());

        // Inyectamos ambos casos de uso
        viewModel = new InvoiceViewModel(getInvoicesUseCase, filterInvoicesUseCase);
    }

    @Test
    public void alIniciar_CargaFacturasLocalmente() {
        verify(getInvoicesUseCase).invoke(any());
    }

    @Test
    public void alActualizarFiltros_AplicaFiltrosCorrectamente() throws InterruptedException {
        // 1. GIVEN: Datos de prueba
        List<Invoice> datosPrueba = new ArrayList<>();
        datosPrueba.add(crearFactura(100.0f, "Pagada"));
        datosPrueba.add(crearFactura(500.0f, "Pendiente"));
        datosPrueba.add(crearFactura(50.0f, "Pagada"));

        // Inyectamos datos manualmente
        viewModel.setFacturasOriginalesTest(datosPrueba);

        // 2. WHEN: Configuramos filtro de importe > 80
        InvoiceFilters filtros = new InvoiceFilters();
        filtros.setImporteMin(80.0);
        filtros.setImporteMax(1000.0);
        // IMPORTANTE: Si tu lógica de filtro requiere estados seleccionados (no null), inicialízalos
        filtros.setEstadosSeleccionados(null); // o una lista vacía si tu lógica lo pide

        // Usamos CountDownLatch para esperar a que el LiveData emita valor
        CountDownLatch latch = new CountDownLatch(1);

        // Observamos el LiveData antes de ejecutar la acción
        viewModel.getFacturas().observeForever(invoices -> {
            // Si recibimos una lista de tamaño 2, liberamos el latch
            // (La primera emisión podría ser la lista completa de 3, así que filtramos por la condición de éxito)
            if (invoices != null && invoices.size() == 2) {
                latch.countDown();
            }
        });

        // Ejecutamos
        viewModel.actualizarFiltros(filtros);

        // Esperamos máximo 2 segundos
        boolean success = latch.await(2, TimeUnit.SECONDS);

        // Verificación final
        List<Invoice> resultado = viewModel.getFacturas().getValue();
        assertNotNull("El LiveData es null", resultado);
        assertEquals("El filtrado no devolvió 2 elementos", 2, resultado.size());
    }



    private Invoice crearFactura(float importe, String estado) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceAmount(importe);
        invoice.setInvoiceStatus(estado);
        invoice.setInvoiceDate(LocalDate.now());
        return invoice;
    }
}
