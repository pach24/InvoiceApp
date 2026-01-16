package com.nexosolar.android.ui.invoices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nexosolar.android.R;
import com.nexosolar.android.core.DateUtils;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.databinding.ItemInvoiceBinding;
import com.nexosolar.android.domain.models.InvoiceState;

import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> listaFacturas;



    @SuppressLint("NotifyDataSetChanged")
    public void setFacturas(List<Invoice> facturas) {
        this.listaFacturas = facturas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceBinding binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new InvoiceViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull InvoiceViewHolder holder, int position) {
        Invoice factura = listaFacturas.get(position);
        Context context = holder.itemView.getContext();
        // 1. Obtenemos el texto ya formateado
        String fechaTexto = DateUtils.formatDate(factura.getFecha());

        // 2. Lo asignamos directamente
        if (!fechaTexto.isEmpty()) {
            holder.binding.txtFecha.setText(fechaTexto);
        } else {
            context = holder.itemView.getContext();
            holder.binding.txtFecha.setText(context.getString(R.string.sin_fecha));
        }

        // Establecer el importe
        holder.binding.txtImporte.setText(String.format(Locale.getDefault(), "%.2f €", factura.getImporteOrdenacion()));

        // Establecer el estado
        InvoiceState estadoEnum = factura.getEstadoEnum();



        switch (estadoEnum) {
            case PENDIENTE:
                holder.binding.txtEstado.setText(R.string.estado_pendiente);
                holder.binding.txtEstado.setTextColor(ContextCompat.getColor(context, R.color.texto_alerta));
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case PAGADA:
                // Si está pagada, ¿queremos ocultarlo o mostrarlo en otro color?
                // Tu código original lo ocultaba:
                holder.binding.txtEstado.setVisibility(View.GONE);
                break;

            case ANULADA:
                holder.binding.txtEstado.setText(R.string.estado_anulada);
                holder.binding.txtEstado.setTextColor(ContextCompat.getColor(context, R.color.texto_alerta));
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case CUOTA_FIJA:
                holder.binding.txtEstado.setText(R.string.estado_cuota_fija);
                holder.binding.txtEstado.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case PLAN_PAGO:
                holder.binding.txtEstado.setText(R.string.estado_plan_pago);
                holder.binding.txtEstado.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            default:
                // Fallback para estados nuevos que no controlamos
                holder.binding.txtEstado.setText(factura.getDescEstado());
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;
        }

        holder.itemView.setOnClickListener(this::showPopup);
    }

    private void showPopup(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setTitle("Información")
                .setMessage("Esta funcionalidad aún no está disponible")
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public int getItemCount() {
        return listaFacturas != null ? listaFacturas.size() : 0;
    }

    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding;

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
