package com.example.pruebas;

import java.util.List;

// Representa la respuesta de la API, en nuestro caso un objeto con el número de facturas y una lista de objetos factura

public class InvoiceResponse {

    private int numFacturas;
    private List<Invoice> facturas;

    public int getNumFacturas() { return numFacturas; }
    public List<Invoice> getFacturas() { return facturas; }

    public double getMaxImporte() {
        double maxImporte = Double.MIN_VALUE; // Inicializamos con el valor más bajo posible

        // Verificamos si la lista de facturas no es null ni vacía
        if (facturas != null && !facturas.isEmpty()) {
            for (Invoice invoice : facturas) {
                if (invoice.getImporteOrdenacion() > maxImporte) {
                    maxImporte = invoice.getImporteOrdenacion(); // Actualizamos el máximo
                }
            }
        }

        return maxImporte; // Devolvemos el importe máximo encontrado
    }
}
