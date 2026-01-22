package com.nexosolar.android.data.remote;

import java.util.List;

// Representa la respuesta de la API, en nuestro caso un objeto con el número de facturas y una lista de objetos factura

public class InvoiceResponse {

    private final int numFacturas;
    private final List<InvoiceDto> facturas;

    public InvoiceResponse(int numFacturas, List<InvoiceDto> facturas) {
        this.numFacturas = numFacturas;
        this.facturas = facturas;
    }

    //No he llegado a usar el número de facturas de momento
    public int getNumFacturas() { return numFacturas; }
    public List<InvoiceDto> getFacturas() { return facturas; }


}
