package com.nexosolar.android.ui.invoices.managers;


import com.nexosolar.android.domain.models.Invoice;

import java.time.LocalDate;
import java.util.List;

/**
 * Responsabilidad: Realizar cálculos estadísticos sobre una lista de facturas.
 * - Extraído de InvoiceFilterManager para cumplir SRP.
 * - Es una clase pura sin estado (Stateless).
 */
public class InvoiceStatisticsCalculator {

    public float calculateMaxAmount(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return 0f;
        }

        float max = 0f;
        for (Invoice invoice : invoices) {
            if (invoice.getInvoiceAmount() > max) {
                max = invoice.getInvoiceAmount();
            }
        }
        return max;
    }

    public LocalDate calculateOldestDate(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return null;
        }

        LocalDate oldest = null;
        for (Invoice invoice : invoices) {
            LocalDate current = invoice.getInvoiceDate();
            if (current != null) {
                if (oldest == null || current.isBefore(oldest)) {
                    oldest = current;
                }
            }
        }
        return oldest;
    }

    public LocalDate calculateNewestDate(List<Invoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return null;
        }

        LocalDate newest = null;
        for (Invoice invoice : invoices) {
            LocalDate current = invoice.getInvoiceDate();
            if (current != null) {
                if (newest == null || current.isAfter(newest)) {
                    newest = current;
                }
            }
        }
        return newest;
    }
}