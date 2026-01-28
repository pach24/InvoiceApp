package com.nexosolar.android.domain;

import static org.junit.Assert.assertEquals;

import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.usecase.invoice.FilterInvoicesUseCase;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tests unitarios para FilterInvoicesUseCase.
 * Valida la lógica de filtrado de facturas por estado, fecha e importe.
 */
public class FilterInvoicesUseCaseTest {

    private FilterInvoicesUseCase useCase;
    private List<Invoice> baseList;

    @Before
    public void setUp() {
        useCase = new FilterInvoicesUseCase();

        // Datos base de prueba
        baseList = new ArrayList<>();
        baseList.add(createInvoice(100f, "Pagada", LocalDate.of(2025, 1, 1)));
        baseList.add(createInvoice(200f, "Pendiente de pago", LocalDate.of(2025, 2, 1)));
        baseList.add(createInvoice(300f, "Anulada", LocalDate.of(2025, 3, 1)));
    }

    @Test
    public void execute_whenFilterByStatus_returnsOnlyMatchingInvoices() {
        // GIVEN: Lista de facturas con diferentes estados
        List<String> statuses = Collections.singletonList("Pagada");

        // WHEN: Filtramos por estado "Pagada"
        List<Invoice> result = useCase.execute(baseList, statuses, null, null, null, null);

        // THEN: Solo retorna facturas con estado "Pagada"
        assertEquals("Debería retornar solo 1 factura con estado Pagada", 1, result.size());
        assertEquals("La factura retornada debería tener estado Pagada",
                "Pagada", result.get(0).getInvoiceStatus());
    }

    @Test
    public void execute_whenFilterByAmountRange_returnsInvoicesInRange() {
        // GIVEN: Lista de facturas con diferentes importes (100, 200, 300)

        // WHEN: Filtramos por rango de importe entre 150 y 250
        List<Invoice> result = useCase.execute(baseList, null, null, null, 150.0, 250.0);

        // THEN: Solo retorna la factura de 200 (única dentro del rango)
        assertEquals("Debería retornar solo 1 factura en el rango 150-250", 1, result.size());
        assertEquals("La factura retornada debería tener importe de 200",
                200f, result.get(0).getInvoiceAmount(), 0.01);
    }

    @Test
    public void execute_whenNoFilters_returnsAllInvoices() {
        // GIVEN: Lista de facturas sin ningún filtro aplicado

        // WHEN: Ejecutamos sin filtros
        List<Invoice> result = useCase.execute(baseList, null, null, null, null, null);

        // THEN: Retorna todas las facturas
        assertEquals("Sin filtros debería retornar todas las facturas", 3, result.size());
    }

    @Test
    public void execute_whenEmptyList_returnsEmptyList() {
        // GIVEN: Lista vacía de facturas
        List<Invoice> emptyList = new ArrayList<>();
        List<String> statuses = Collections.singletonList("Pagada");

        // WHEN: Filtramos una lista vacía
        List<Invoice> result = useCase.execute(emptyList, statuses, null, null, null, null);

        // THEN: Retorna lista vacía
        assertEquals("Una lista vacía debería retornar lista vacía", 0, result.size());
    }

    @Test
    public void execute_whenFilterByDate_returnsInvoicesInDateRange() {
        // GIVEN: Lista de facturas con diferentes fechas
        LocalDate startDate = LocalDate.of(2025, 1, 15);
        LocalDate endDate = LocalDate.of(2025, 2, 15);

        // WHEN: Filtramos por rango de fechas (solo la segunda factura debería cumplir)
        List<Invoice> result = useCase.execute(baseList, null, startDate, endDate, null, null);

        // THEN: Solo retorna facturas dentro del rango de fechas
        assertEquals("Debería retornar solo facturas en el rango de fechas", 1, result.size());
        assertEquals("La factura retornada debería ser la del 1 de febrero",
                LocalDate.of(2025, 2, 1), result.get(0).getInvoiceDate());
    }

    @Test
    public void execute_whenMultipleFilters_returnsInvoicesMeetingAllCriteria() {
        // GIVEN: Lista de facturas y múltiples filtros simultáneos
        List<String> statuses = Collections.singletonList("Pagada");
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);
        Double minAmount = 50.0;
        Double maxAmount = 150.0;

        // WHEN: Aplicamos múltiples filtros (estado + fecha + importe)
        List<Invoice> result = useCase.execute(baseList, statuses, startDate, endDate, minAmount, maxAmount);

        // THEN: Solo retorna facturas que cumplen TODOS los criterios
        assertEquals("Debería retornar facturas que cumplen todos los filtros", 1, result.size());
        Invoice invoice = result.get(0);
        assertEquals("Debería tener estado Pagada", "Pagada", invoice.getInvoiceStatus());
        assertEquals("Debería tener importe de 100", 100f, invoice.getInvoiceAmount(), 0.01);
        assertEquals("Debería tener fecha de enero 2025",
                LocalDate.of(2025, 1, 1), invoice.getInvoiceDate());
    }

    @Test
    public void execute_whenFilterByMultipleStatuses_returnsMatchingInvoices() {
        // GIVEN: Lista de facturas con diferentes estados
        List<String> statuses = new ArrayList<>();
        statuses.add("Pagada");
        statuses.add("Anulada");

        // WHEN: Filtramos por múltiples estados
        List<Invoice> result = useCase.execute(baseList, statuses, null, null, null, null);

        // THEN: Retorna facturas con cualquiera de los estados especificados
        assertEquals("Debería retornar 2 facturas (Pagada y Anulada)", 2, result.size());
    }

    // ========== Método auxiliar ==========

    /**
     * Crea una factura de prueba con los parámetros dados.
     */
    private Invoice createInvoice(float amount, String status, LocalDate date) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceAmount(amount);
        invoice.setInvoiceStatus(status);
        invoice.setInvoiceDate(date);
        return invoice;
    }
}
