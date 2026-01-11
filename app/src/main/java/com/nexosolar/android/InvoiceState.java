package com.nexosolar.android;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

public enum InvoiceState {
    PENDIENTE("Pendiente de pago", R.string.pendientes_de_pago, R.color.texto_alerta),
    PAGADA("Pagada", R.string.pagadas, R.color.texto_normal),
    ANULADA("Anulada", R.string.anuladas, R.color.texto_alerta),
    CUOTA_FIJA("Cuota fija", R.string.cuota_fija, R.color.texto_normal),
    PLAN_PAGO("Plan de pago", R.string.plan_de_pago, R.color.texto_normal),
    DESCONOCIDO("", R.string.estado, R.color.texto_normal); // Valor por defecto

    private final String textoServidor;
    private final int resIdTexto;
    private final int resIdColor;

    // El constructor debe tener el mismo nombre que el Enum (InvoiceState)
    InvoiceState(String textoServidor, @StringRes int resIdTexto, @ColorRes int resIdColor) {
        this.textoServidor = textoServidor;
        this.resIdTexto = resIdTexto;
        this.resIdColor = resIdColor;
    }

    public int getResIdTexto() {
        return resIdTexto;
    }

    public int getResIdColor() {
        return resIdColor;
    }

    // Método Factory corregido para devolver InvoiceState
    public static InvoiceState fromTextoServidor(String texto) {
        if (texto == null) return DESCONOCIDO;

        for (InvoiceState estado : values()) {
            // Usamos equalsIgnoreCase para ser tolerantes a mayúsculas/minúsculas
            if (estado.textoServidor.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        return DESCONOCIDO;
    }
}
