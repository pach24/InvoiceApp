package com.example.pruebas;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pruebas.databinding.ItemInvoiceBinding; // Asegúrate de importar el binding correcto
import java.util.List;

public class InvoiceAdapter extends RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder> {
    private List<Invoice> listaFacturas;

    // Metodo para actualizar la lista de facturas
    @SuppressLint("NotifyDataSetChanged")
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
        holder.bind(factura); // Llamar al metodo de enlace en el ViewHolder
    }

    @Override
    public int getItemCount() {
        return (listaFacturas != null) ? listaFacturas.size() : 0; // Devolver el tamaño de la lista
    }

    // Clase interna que representa la vista de cada factura en la lista
    public static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        private final ItemInvoiceBinding binding; // Agregar una instancia de ItemInvoiceBinding

        public InvoiceViewHolder(ItemInvoiceBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // Asignar el binding a la instancia
        }

        public void bind(Invoice factura) {
            binding.txtFecha.setText(factura.getFecha());
            binding.txtImporte.setText("€ " + factura.getImporteOrdenacion());
            binding.txtEstado.setText(factura.getDescEstado());
        }
    }
}
