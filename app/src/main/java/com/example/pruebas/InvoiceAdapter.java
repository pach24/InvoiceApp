package com.example.pruebas;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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
    public void setFacturas(List<Invoice> facturas) {
        this.listaFacturas = facturas;
        notifyDataSetChanged();  // Notifica a RecyclerView que los datos cambiaron
    }

    @Override
    public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInvoiceBinding binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new InvoiceViewHolder(binding);
    }

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

            default:
                holder.binding.txtEstado.setVisibility(View.GONE); // Ocultar en otros casos
                break;
        }

        // Acción al hacer clic en el ítem
        holder.itemView.setOnClickListener(this::showPopup);
    }


    // Método para mostrar un popup nativo
    private void showPopup(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setTitle("Información")
                .setMessage("Esta funcionalidad aún no está disponible")
                .setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Método para formatear la fecha
    private String formatFecha(String fecha) {
        try {
            // Formato de la fecha original
            Log.d("entréeeeeeeeeeeee", "entréeeeeeeeeeeeeeeeeeee");
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
        private final ItemInvoiceBinding binding; // Agregar una instancia de ItemInvoiceBinding

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // Asignar el binding a la instancia
        }

        public void bind(Invoice factura) {
            binding.txtImporte.setText("€ " + factura.getImporteOrdenacion());
            binding.txtEstado.setText(factura.getDescEstado());
        }
    }
}
