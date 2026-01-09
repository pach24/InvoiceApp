package com.example.pruebas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pruebas.databinding.FragmentFilterBinding;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment {
    private FragmentFilterBinding binding;
    private InvoiceViewModel viewModel;

    // Variables para guardar la selección (ahora como Long)
    private Long fechaInicioMillis = null;
    private Long fechaFinMillis = null;

    // Para visualizar fechas en los botones
    private final DateTimeFormatter buttonFormatter = DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.SHORT);

    private float maxImporte;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(InvoiceViewModel.class);

        // 1. Recuperar argumentos (maxImporte y fecha antigua)
        Bundle args = getArguments();
        if (args != null) {
            maxImporte = args.getFloat("MAX_IMPORTE", 100f);

            // Recuperamos fecha más antigua (como long) para inicializar el "Desde"
            // Nota: Si en InvoiceListActivity aún pasas String, deberás actualizarlo allí también.
            // Por compatibilidad, si recibimos 0 o nada, usaremos null (hoy/default).
            long oldestMillis = args.getLong("OLDEST_DATE_MILLIS", 0);
            if (oldestMillis != 0) {
                fechaInicioMillis = oldestMillis;
            }
        }

        // 2. Configurar Textos Iniciales de Fecha
        if (fechaInicioMillis != null) {
            binding.btnSelectDate.setText(millisToLocalDate(fechaInicioMillis).format(buttonFormatter));
        } else {
            // Si no hay fecha antigua, ponemos la de hoy
            fechaInicioMillis = localDateToMillis(LocalDate.now());
            binding.btnSelectDate.setText(millisToLocalDate(fechaInicioMillis).format(buttonFormatter));
        }

        // Fecha fin por defecto: Hoy
        fechaFinMillis = localDateToMillis(LocalDate.now());
        binding.btnSelectDateUntil.setText(millisToLocalDate(fechaFinMillis).format(buttonFormatter));


        // 3. Configurar Sliders
        if (maxImporte > 0) {
            binding.rangeSlider.setValueFrom(0f);
            binding.rangeSlider.setValueTo(maxImporte);
            binding.rangeSlider.setValues(0f, maxImporte);
            binding.tvMinValue.setText("0 €");
            binding.tvMaxValue.setText(String.format("%.02f €", maxImporte));
            binding.tvMaxImporte.setText(String.format("%.02f €", maxImporte));
        } else {
            // Fallback por si maxImporte es 0
            binding.rangeSlider.setValues(0f, 100f);
            binding.tvMaxValue.setText("0 €");
        }

        // Listener Slider
        binding.rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() == 2) {
                float currentMax = values.get(1);
                binding.tvMinValue.setText(String.format("%.0f €", values.get(0)));

                // Pequeño ajuste visual si está cerca del tope
                if (currentMax >= maxImporte - 0.001f) {
                    binding.tvMaxValue.setText(String.format("%.2f €", currentMax));
                } else {
                    binding.tvMaxValue.setText(String.format("%.0f €", currentMax));
                }
            }
        });

        // 4. Configurar Checkboxes iniciales
        binding.checkPendientesPago.setChecked(true);
        binding.checkPagadas.setChecked(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Listeners de Fecha (Ahora usan MaterialDatePicker)
        binding.btnSelectDate.setOnClickListener(v -> openMaterialDatePicker(true));
        binding.btnSelectDateUntil.setOnClickListener(v -> openMaterialDatePicker(false));

        // Botón Aplicar
        binding.btnAplicar.setOnClickListener(v -> {
            // Validación de lógica fechas (simple comparación de longs)
            if (fechaInicioMillis != null && fechaFinMillis != null) {
                if (fechaInicioMillis > fechaFinMillis) {
                    Toast.makeText(requireContext(),
                            "La fecha de inicio no puede ser posterior a la final.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Recoger valores del Slider
            List<Float> valoresSlider = binding.rangeSlider.getValues();
            float importeMin = valoresSlider.get(0);
            float importeMax = valoresSlider.get(1);

            // Crear Bundle con datos tipados (Longs y Floats)
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("ESTADOS", new ArrayList<>(getStrings()));

            if (fechaInicioMillis != null) bundle.putLong("FECHA_INICIO_MILLIS", fechaInicioMillis);
            if (fechaFinMillis != null) bundle.putLong("FECHA_FIN_MILLIS", fechaFinMillis);

            bundle.putDouble("IMPORTE_MIN", importeMin);
            bundle.putDouble("IMPORTE_MAX", importeMax);

            // Comunicar a Activity
            InvoiceListActivity activity = (InvoiceListActivity) getActivity();
            if (activity != null) {
                // OJO: Asegúrate que Activity.aplicarFiltros() sepa leer estos Longs
                boolean hayResultados = activity.aplicarFiltros(bundle);

                if (!hayResultados) {
                    Toast.makeText(requireContext(),
                            "No se encontraron resultados.", Toast.LENGTH_SHORT).show();
                    return;
                }
                activity.restoreMainView();
            }
            getParentFragmentManager().popBackStack();
        });

        // Botón Cerrar
        binding.btnCerrar.setOnClickListener(v -> {
            if (getActivity() instanceof InvoiceListActivity) {
                ((InvoiceListActivity) getActivity()).restoreMainView();
            }
            getParentFragmentManager().popBackStack();
        });

        // Botón Borrar
        binding.btnBorrar.setOnClickListener(v -> resetFilters());
    }

    /**
     * Nuevo método para abrir el calendario moderno
     */
    private void openMaterialDatePicker(boolean isStartDate) {
        long selection = isStartDate
                ? (fechaInicioMillis != null ? fechaInicioMillis : MaterialDatePicker.todayInUtcMilliseconds())
                : (fechaFinMillis != null ? fechaFinMillis : MaterialDatePicker.todayInUtcMilliseconds());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStartDate ? "Seleccionar Inicio" : "Seleccionar Fin")
                .setSelection(selection)
                .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedMillis -> {
            // Guardamos el Long seleccionado directamente
            if (isStartDate) {
                fechaInicioMillis = selectedMillis;
                binding.btnSelectDate.setText(millisToLocalDate(selectedMillis).format(buttonFormatter));
            } else {
                fechaFinMillis = selectedMillis;
                binding.btnSelectDateUntil.setText(millisToLocalDate(selectedMillis).format(buttonFormatter));
            }
        });

        datePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
    }

    private void resetFilters() {
        // Resetear fechas a Hoy / Null según prefieras
        fechaInicioMillis = localDateToMillis(LocalDate.now());
        fechaFinMillis = localDateToMillis(LocalDate.now());

        binding.btnSelectDate.setText(millisToLocalDate(fechaInicioMillis).format(buttonFormatter));
        binding.btnSelectDateUntil.setText(millisToLocalDate(fechaFinMillis).format(buttonFormatter));

        // Resetear Slider
        binding.rangeSlider.setValues(0f, maxImporte > 0 ? maxImporte : 100f);

        // Resetear Checkboxes
        binding.checkPagadas.setChecked(false);
        binding.checkAnuladas.setChecked(false);
        binding.checkCuotaFija.setChecked(false);
        binding.checkPendientesPago.setChecked(false);
        binding.checkPlanPago.setChecked(false);
    }

    @NonNull
    private List<String> getStrings() {
        List<String> estados = new ArrayList<>();
        if (binding.checkPagadas.isChecked()) estados.add("Pagada");
        if (binding.checkPendientesPago.isChecked()) estados.add("Pendiente de pago");
        if (binding.checkAnuladas.isChecked()) estados.add("Anulada");
        if (binding.checkCuotaFija.isChecked()) estados.add("Cuota fija");
        if (binding.checkPlanPago.isChecked()) estados.add("Plan de pago");
        return estados;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Evitamos fugas de memoria del binding
        binding = null;
    }

    // --- MÉTODOS AUXILIARES FECHAS ---

    private Long localDateToMillis(LocalDate localDate) {
        if (localDate == null) return null;
        return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    }

    private LocalDate millisToLocalDate(Long millis) {
        if (millis == null) return null;
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
