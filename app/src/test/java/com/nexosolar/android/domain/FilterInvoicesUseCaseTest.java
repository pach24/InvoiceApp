package com.nexosolar.android.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilterInvoicesUseCaseTest {

    private FilterInvoicesUseCase useCase;
    private List<Invoice> listaBase;

    @Before
    public void setUp() {
        useCase = new FilterInvoicesUseCase();

        // Datos base
        listaBase = new ArrayList<>();
        listaBase.add(crearFactura(100f, "Pagada", LocalDate.of(2025, 1, 1)));
        listaBase.add(crearFactura(200f, "Pendiente de pago", LocalDate.of(2025, 2, 1)));
        listaBase.add(crearFactura(300f, "Anulada", LocalDate.of(2025, 3, 1)));
    }

    @Test
    public void filtrarPorEstado_DevuelveSoloCoincidentes() {
        List<String> estados = Collections.singletonList("Pagada");

        List<Invoice> result = useCase.execute(listaBase, estados, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("Pagada", result.get(0).getInvoiceStatus());
    }

    @Test
    public void filtrarPorImporte_DevuelveRangoCorrecto() {
        // Filtrar entre 150 y 250 (debería quedar solo la de 200)
        List<Invoice> result = useCase.execute(listaBase, null, null, null, 150.0, 250.0);

        assertEquals(1, result.size());
        assertEquals(200f, result.get(0).getInvoiceAmount(), 0.01);
    }

    @Test
    public void sinFiltros_DevuelveTodo() {
        List<Invoice> result = useCase.execute(listaBase, null, null, null, null, null);
        assertEquals(3, result.size());
    }

    private Invoice crearFactura(float importe, String estado, LocalDate fecha) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceAmount(importe);
        invoice.setInvoiceStatus(estado);
        invoice.setInvoiceDate(fecha);
        return invoice;
    }
}
