package com.nexosolar.android.domain.models;

/**
 * Enum que define los posibles estados de una factura en el sistema.
 * Actúa como adaptador entre los textos del servidor y la lógica de negocio/UI.
 */
public enum InvoiceState {

    PENDIENTE("Pendiente de pago"),
    PAGADA("Pagada"),
    ANULADA("Anulada"),
    CUOTA_FIJA("Cuota fija"),
    PLAN_PAGO("Plan de pago"),
    DESCONOCIDO("");

    // ===== Variables de instancia =====
    private final String textoServidor;

    // ===== Constructores =====

    InvoiceState(String textoServidor) {
        this.textoServidor = textoServidor;
    }

    // ===== Getters y Setters =====

    public String getTextoServidor() {
        return textoServidor;
    }

    // ===== Métodos públicos =====

    /**
     * Mapea un string arbitrario del servidor al enum correspondiente.
     * Case-insensitive para mayor robustez ante cambios menores en API.
     */
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
