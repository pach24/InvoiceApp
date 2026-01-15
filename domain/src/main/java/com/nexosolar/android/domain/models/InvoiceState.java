package com.nexosolar.android.domain.models;

public enum InvoiceState {
    PENDIENTE("Pendiente de pago"),
    PAGADA("Pagada"),
    ANULADA("Anulada"),
    CUOTA_FIJA("Cuota fija"),
    PLAN_PAGO("Plan de pago"),
    DESCONOCIDO("");

    private final String textoServidor;

    InvoiceState(String textoServidor) {
        this.textoServidor = textoServidor;
    }

    public String getTextoServidor() {
        return textoServidor;
    }

    public static InvoiceState fromTextoServidor(String texto) {
        if (texto == null) return DESCONOCIDO;
        for (InvoiceState estado : values()) {
            if (estado.textoServidor.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        return DESCONOCIDO;
    }
}
