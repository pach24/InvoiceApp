package com.nexosolar.android.ui.utils;

import com.nexosolar.android.R;
import com.nexosolar.android.domain.models.InvoiceState;

public class InvoiceStateMapper {

    public static int getColorRes(InvoiceState state) {
        switch (state) {
            case PENDIENTE:
            case ANULADA:
                return R.color.texto_alerta; // Tu color rojo
            case PAGADA:
            case CUOTA_FIJA:
            case PLAN_PAGO:
            default:
                return R.color.texto_normal; // Tu color normal
        }
    }

    public static int getTextRes(InvoiceState state) {
        switch (state) {
            case PENDIENTE: return R.string.pendientes_de_pago;
            case PAGADA:    return R.string.pagadas;
            case ANULADA:   return R.string.anuladas;
            case CUOTA_FIJA:return R.string.cuota_fija;
            case PLAN_PAGO: return R.string.plan_de_pago;
            default:        return R.string.estado;
        }
    }
}
