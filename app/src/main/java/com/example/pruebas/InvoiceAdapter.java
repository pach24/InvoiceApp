package com.example.pruebas;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pruebas.databinding.ItemInvoiceBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private List<Invoice> listaFacturas;

    // Método para actualizar la lista de facturas
    @SuppressLint("NotifyDataSetChanged")
    public void setFacturas(List<Invoice> facturas) {
        this.listaFacturas = facturas;
        notifyDataSetChanged();  // Notifica a RecyclerView que los datos cambiaron
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

        // Formatear la fecha
        String fechaFormateada = formatFecha(factura.getFecha());
        holder.binding.txtFecha.setText(fechaFormateada);

        // Establecer el importe con formato correcto
        holder.binding.txtImporte.setText(String.format(Locale.getDefault(), "%.2f €", factura.getImporteOrdenacion()));

        // Configurar el estado
        String estado = factura.getDescEstado();

        switch (estado) {
            case "Pendiente de pago":
                holder.binding.txtEstado.setText("Pendiente de pago");
                holder.binding.txtEstado.setTextColor(Color.RED); // Cambiar el texto a rojo
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case "Pagada":
                holder.binding.txtEstado.setVisibility(View.GONE); // Ocultar si está pagada
                break;

            case "Anulada":
                holder.binding.txtEstado.setText("Anulada");
                holder.binding.txtEstado.setTextColor(Color.RED); // Cambiar el texto a rojo
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case "Cuota fija":
                holder.binding.txtEstado.setText("Cuota fija");
                holder.binding.txtEstado.setTextColor(Color.BLACK);
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            case "Plan de pago":
                holder.binding.txtEstado.setText("Plan de pago");
                holder.binding.txtEstado.setTextColor(Color.BLACK); // Verde claro
                holder.binding.txtEstado.setVisibility(View.VISIBLE);
                break;

            default:
                holder.binding.txtEstado.setVisibility(View.VISIBLE); // Ocultar en otros casos
                break;
        }

        // Acción al hacer clic en el ítem
        holder.itemView.setOnClickListener(this::showPopup);
    }


    // Metodo para mostrar un popup nativo
    // TODO Se configura con el tema que use el dispositivo (puede usar tema oscuro) Manejar esto.
    private void showPopup(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setTitle("Información")
                .setMessage("Esta funcionalidad aún no está disponible")
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Metodo para formatear la fecha
    private String formatFecha(String fecha) {
        try {
            // Formato de la fecha original
            SimpleDateFormat originalFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Nuevo formato
            SimpleDateFormat newFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            // Parseamos la fecha original y la convertimos al nuevo formato
            return newFormat.format(Objects.requireNonNull(originalFormat.parse(fecha)));
        } catch (Exception e) {
            return fecha; // Si hay un error en el formato, regresamos la fecha original
        }
    }

    @Override
    public int getItemCount() {
        if (listaFacturas != null) {
            return listaFacturas.size();
        } else {
            return 0;
        }
    }

    // Clase interna que representa la vista de cada factura en la lista
    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding;

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // Asignar el binding a la instancia
        }

    }
}
