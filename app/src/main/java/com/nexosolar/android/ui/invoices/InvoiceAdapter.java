package com.nexosolar.android.ui.invoices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nexosolar.android.R;
import com.nexosolar.android.core.DateUtils;
import com.nexosolar.android.databinding.ItemInvoiceBinding;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.domain.models.InvoiceState;

import java.util.List;
import java.util.Locale;

/**
 * InvoiceAdapter
 *
 * Adaptador de RecyclerView para mostrar la lista de facturas.
 * Cada item muestra la fecha, importe y estado visual de una factura.
 * Maneja estados diferenciados con colores y visibilidad condicional.
 */
public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    // ===== Variables de instancia =====

    private List<Invoice> listaFacturas;

    // ===== Métodos públicos =====

    @SuppressLint("NotifyDataSetChanged")
    public void setFacturas(List<Invoice> facturas) {
        this.listaFacturas = facturas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceBinding binding = ItemInvoiceBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new InvoiceViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice factura = listaFacturas.get(position);
        Context context = holder.itemView.getContext();

        bindFecha(holder, factura, context);
        bindImporte(holder, factura);
        bindEstado(holder, factura, context);

        holder.itemView.setOnClickListener(this::showPopup);
    }

    @Override
    public int getItemCount() {
        return listaFacturas != null ? listaFacturas.size() : 0;
    }

    // ===== Métodos privados de binding =====

    /**
     * Vincula la fecha de la factura al TextView correspondiente.
     * Si no hay fecha válida, muestra un placeholder.
     */
    private void bindFecha(InvoiceViewHolder holder, Invoice factura, Context context) {
        String fechaTexto = DateUtils.formatDate(factura.getInvoiceDate());
        if (!fechaTexto.isEmpty()) {
            holder.binding.txtFecha.setText(fechaTexto);
        } else {
            holder.binding.txtFecha.setText(context.getString(R.string.sin_fecha));
        }
    }

    /**
     * Vincula el importe de la factura con formato de moneda (EUR).
     */
    private void bindImporte(InvoiceViewHolder holder, Invoice factura) {
        holder.binding.txtImporte.setText(
                String.format(Locale.getDefault(), "%.2f €", factura.getInvoiceAmount())
        );
    }

    /**
     * Vincula el estado de la factura con texto y color apropiados.
     * Gestiona visibilidad condicional según el tipo de estado (pagada se oculta, resto visible).
     */
    private void bindEstado(InvoiceViewHolder holder, Invoice factura, Context context) {
        InvoiceState estadoEnum = factura.getEstadoEnum();

        switch (estadoEnum) {
            case PENDING:
                holder.binding.txtEstado.setText(R.string.estado_pendiente);
                holder.binding.txtEstado.setTextColor(
                        ContextCompat.getColor(context, R.color.texto_alerta)
                );
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case PAID:
                holder.binding.txtEstado.setVisibility(View.GONE);
                break;

            case CANCELLED:
                holder.binding.txtEstado.setText(R.string.estado_anulada);
                holder.binding.txtEstado.setTextColor(
                        ContextCompat.getColor(context, R.color.texto_alerta)
                );
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case FIXED_FEE:
                holder.binding.txtEstado.setText(R.string.estado_cuota_fija);
                holder.binding.txtEstado.setTextColor(
                        ContextCompat.getColor(context, android.R.color.black)
                );
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case PAYMENT_PLAN:
                holder.binding.txtEstado.setText(R.string.estado_plan_pago);
                holder.binding.txtEstado.setTextColor(
                        ContextCompat.getColor(context, android.R.color.black)
                );
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            default:
                holder.binding.txtEstado.setText(factura.getInvoiceStatus());
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Muestra un diálogo informativo al hacer click en un item.
     * Funcionalidad de detalle pendiente de implementación.
     */
    private void showPopup(View view) {
        new AlertDialog.Builder(view.getContext())
                .setTitle("Información")
                .setMessage("Esta funcionalidad aún no está disponible")
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ===== ViewHolder interno =====

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding;

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
