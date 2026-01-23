package com.nexosolar.android.domain.models;

/**
 * Enum que define los posibles estados de una factura en el sistema.
 * Actúa como adaptador entre los textos del servidor y la lógica de negocio/UI.
 */
public enum InvoiceState {

    PENDING("Pendiente de pago"),
    PAID("Pagada"),
    CANCELLED("Anulada"),
    FIXED_FEE("Cuota fija"),
    PAYMENT_PLAN("Plan de pago"),
    UNKNONWN("");

    // ===== Variables de instancia =====
    private final String serverValue;

    // ===== Constructores =====

    InvoiceState(String serverValue) {
        this.serverValue = serverValue;
    }

    // ===== Getters y Setters =====

    public String getServerValue() {
        return serverValue;
    }

    // ===== Métodos públicos =====

    /**
     * Mapea un string arbitrario del servidor al enum correspondiente.
     * Case-insensitive para mayor robustez ante cambios menores en API.
     */
    public static InvoiceState fromServerValue(String value) {
        if (value == null) return UNKNONWN;

        for (InvoiceState state : values()) {
            if (state.serverValue.equalsIgnoreCase(value)) {
                return state;
            }
        }
        return UNKNONWN;
    }
}
