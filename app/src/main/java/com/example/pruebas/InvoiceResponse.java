package com.example.pruebas;

import java.util.List;

// Representa la respuesta de la API, en nuestro caso un objeto con el número de facturas y una lista de objetos factura

public class InvoiceResponse {

    private int numFacturas;
    private List<Invoice> facturas;

    public int getNumFacturas() { return numFacturas; }
    public List<Invoice> getFacturas() { return facturas; }

}
