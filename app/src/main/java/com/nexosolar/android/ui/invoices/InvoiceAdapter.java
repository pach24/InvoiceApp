package com.nexosolar.android.ui.invoices;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nexosolar.android.R;
import com.nexosolar.android.core.DateUtils;
import com.nexosolar.android.domain.models.Invoice;
import com.nexosolar.android.databinding.ItemInvoiceBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {

    private List<Invoice> listaFacturas;

    // Formateador reutilizable para convertir LocalDate a String (Ej: 25 Ene 2024)
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM);

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

        // 1. Obtenemos el texto ya formateado
        String fechaTexto = DateUtils.formatDate(factura.getFecha());

        // 2. Lo asignamos directamente
        if (fechaTexto != null && !fechaTexto.isEmpty()) {
            holder.binding.txtFecha.setText(fechaTexto);
        } else {
            Context context = holder.itemView.getContext();
            holder.binding.txtFecha.setText(context.getString(R.string.sin_fecha));
        }

        // Establecer el importe
        holder.binding.txtImporte.setText(String.format(Locale.getDefault(), "%.2f €", factura.getImporteOrdenacion()));

        // Configurar estado
        String estado = factura.getDescEstado();
        if (estado == null) estado = "";

        switch (estado) {
            case "Pendiente de pago":
                holder.binding.txtEstado.setText("Pendiente de pago");
                holder.binding.txtEstado.setTextColor(Color.RED);
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;
            case "Pagada":
                holder.binding.txtEstado.setVisibility(View.GONE);
                break;
            case "Anulada":
                holder.binding.txtEstado.setText("Anulada");
                holder.binding.txtEstado.setTextColor(Color.RED);
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;
            case "Cuota fija":
                holder.binding.txtEstado.setText("Cuota fija");
                holder.binding.txtEstado.setTextColor(Color.BLACK);
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;
            case "Plan de pago":
                holder.binding.txtEstado.setText("Plan de pago");
                holder.binding.txtEstado.setTextColor(Color.BLACK);
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;
            default:
                holder.binding.txtEstado.setText(estado);
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
