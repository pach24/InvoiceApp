package com.nexosolar.android.ui.invoices;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.nexosolar.android.databinding.FragmentFilterBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.nexosolar.android.domain.models.InvoiceFilters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento para aplicar filtros a las facturas.
 * Ahora sigue MVVM correctamente: solo maneja la UI y observa el ViewModel.
 */
public class FilterFragment extends Fragment {

    private FragmentFilterBinding binding;
    private InvoiceViewModel viewModel;
    private final DateTimeFormatter buttonFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(InvoiceViewModel.class);

        // Inicializar filtros con valores por defecto
        viewModel.inicializarFiltros();

        setupObservers();
        setupListeners();

        return binding.getRoot();
    }

    /**
     * Configura los observadores del ViewModel
     */
    private void setupObservers() {
        // Observar los filtros actuales del ViewModel
        viewModel.getFiltrosActuales().observe(getViewLifecycleOwner(), this::actualizarUI);

        // Observar errores de validación
        viewModel.getErrorValidacion().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Actualiza la UI según el estado de los filtros en el ViewModel
     */
    @SuppressLint("DefaultLocale")
    private void actualizarUI(InvoiceFilters filtros) {
        if (filtros == null) return;

        // Actualizar fechas
        if (filtros.getFechaInicio() != null) {
            binding.btnSelectDate.setText(filtros.getFechaInicio().format(buttonFormatter));
        }
        if (filtros.getFechaFin() != null) {
            binding.btnSelectDateUntil.setText(filtros.getFechaFin().format(buttonFormatter));
        }

        // Actualizar slider
        float maxImporte = viewModel.getMaxImporte();
        if (maxImporte > 0) {
            binding.rangeSlider.setValueFrom(0f);
            binding.rangeSlider.setValueTo(maxImporte);
            binding.rangeSlider.setValues(
                    filtros.getImporteMin().floatValue(),
                    filtros.getImporteMax().floatValue()
            );
            binding.tvMaxImporte.setText(String.format("%.2f €", maxImporte));
            binding.tvMinValue.setText(String.format("%.0f €", filtros.getImporteMin()));
            binding.tvMaxValue.setText(String.format("%.0f €", filtros.getImporteMax()));
        }

        // Actualizar checkboxes
        List<String> estados = filtros.getEstadosSeleccionados();
        binding.checkPagadas.setChecked(estados.contains("Pagada"));
        binding.checkPendientesPago.setChecked(estados.contains("Pendiente de pago"));
        binding.checkAnuladas.setChecked(estados.contains("Anulada"));
        binding.checkCuotaFija.setChecked(estados.contains("Cuota fija"));
        binding.checkPlanPago.setChecked(estados.contains("Plan de pago"));
    }

    /**
     * Configura los listeners de los elementos de UI
     */
    @SuppressLint("DefaultLocale")
    private void setupListeners() {
        // Date pickers
        binding.btnSelectDate.setOnClickListener(v -> abrirDatePicker(true));
        binding.btnSelectDateUntil.setOnClickListener(v -> abrirDatePicker(false));

        // Slider listener
        binding.rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() == 2) {
                float min = values.get(0);
                float max = values.get(1);
                binding.tvMinValue.setText(String.format("%.0f €", min));

                // Ajuste visual si está cerca del tope
                float maxImporte = viewModel.getMaxImporte();
                if (max >= maxImporte - 0.001f) {
                    binding.tvMaxValue.setText(String.format("%.2f €", max));
                } else {
                    binding.tvMaxValue.setText(String.format("%.0f €", max));
                }
            }
        });

        // Botón aplicar
        binding.btnAplicar.setOnClickListener(v -> {
            InvoiceFilters nuevosFiltros = construirFiltrosDesdeUI();
            viewModel.actualizarFiltros(nuevosFiltros);

            // Solo cerrar si los filtros son válidos
            if (nuevosFiltros.isValid()) {
                cerrarFragmento();
            }
        });

        // Botón borrar
        binding.btnBorrar.setOnClickListener(v -> viewModel.resetearFiltros());

        // Botón cerrar
        binding.btnCerrar.setOnClickListener(v -> cerrarFragmento());
    }

    /**
     * Construye un objeto InvoiceFilters desde el estado actual de la UI
     */
    private InvoiceFilters construirFiltrosDesdeUI() {
        InvoiceFilters filtros = new InvoiceFilters();

        // Recoger estados de los checkboxes
        List<String> estados = new ArrayList<>();
        if (binding.checkPagadas.isChecked()) estados.add("Pagada");
        if (binding.checkPendientesPago.isChecked()) estados.add("Pendiente de pago");
        if (binding.checkAnuladas.isChecked()) estados.add("Anulada");
        if (binding.checkCuotaFija.isChecked()) estados.add("Cuota fija");
        if (binding.checkPlanPago.isChecked()) estados.add("Plan de pago");
        filtros.setEstadosSeleccionados(estados);

        // Recoger fechas del ViewModel actual (ya están actualizadas)
        InvoiceFilters filtrosActuales = viewModel.getFiltrosActuales().getValue();
        if (filtrosActuales != null) {
            filtros.setFechaInicio(filtrosActuales.getFechaInicio());
            filtros.setFechaFin(filtrosActuales.getFechaFin());
        }

        // Recoger importes del slider
        List<Float> valores = binding.rangeSlider.getValues();
        filtros.setImporteMin((double) valores.get(0));
        filtros.setImporteMax((double) valores.get(1));

        return filtros;
    }

    /**
     * Abre el date picker para seleccionar fecha de inicio o fin
     */
    private void abrirDatePicker(boolean esInicio) {
        InvoiceFilters filtrosActuales = viewModel.getFiltrosActuales().getValue();
        if (filtrosActuales == null) return;

        LocalDate fechaActual = esInicio ? filtrosActuales.getFechaInicio() : filtrosActuales.getFechaFin();

        long selection = fechaActual != null
                ? fechaActual.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(esInicio ? "Seleccionar Inicio" : "Seleccionar Fin")
                .setSelection(selection)
                .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedMillis -> {
            LocalDate nuevaFecha = Instant.ofEpochMilli(selectedMillis)
                    .atZone(ZoneOffset.UTC).toLocalDate();

            // Obtenemos la referencia actual
            InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();
            if (filtros != null) {
                if (esInicio) {
                    filtros.setFechaInicio(nuevaFecha);
                } else {
                    filtros.setFechaFin(nuevaFecha);
                }

                // CORRECCIÓN: Usamos el método público del ViewModel
                viewModel.actualizarEstadoFiltros(filtros);
            }
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    /**
     * Cierra el fragmento y restaura la vista principal
     */
    private void cerrarFragmento() {
        if (getActivity() instanceof InvoiceListActivity) {
            ((InvoiceListActivity) getActivity()).restoreMainView();
        }
        getParentFragmentManager().popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
