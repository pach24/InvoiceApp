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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.nexosolar.android.R;
import com.nexosolar.android.core.DateUtils;
import com.nexosolar.android.databinding.FragmentFilterBinding;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.models.InvoiceState;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * FilterFragment
 *
 * Fragmento modal que permite aplicar filtros avanzados sobre las facturas.
 * Ofrece filtrado por:
 * - Rango de fechas (inicio y fin)
 * - Rango de importes (slider)
 * - Estados de factura (checkboxes múltiples)
 *
 * Comportamiento de fechas:
 * - UI: Muestra "día/mes/año" cuando no hay fecha explícitamente seleccionada.
 * - Lógica interna: Usa las fechas extremas reales (más antigua/más nueva)
 *   para evitar filtros vacíos innecesarios.
 *
 * Sigue arquitectura MVVM: solo maneja UI y observa el ViewModel compartido.
 */
public class FilterFragment extends Fragment {

    // ===== Variables de instancia =====

    private FragmentFilterBinding binding;
    private InvoiceViewModel viewModel;


    // ===== Ciclo de vida =====

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(InvoiceViewModel.class);

        //viewModel.resetearFiltros();
        viewModel.inicializarFiltros();

        setupObservers();
        setupListeners();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ===== Configuración de observadores =====

    /**
     * Configura observadores de LiveData para sincronizar UI con el estado del ViewModel.
     */
    private void setupObservers() {
        viewModel.getFiltrosActuales().observe(getViewLifecycleOwner(), this::actualizarUI);

        viewModel.getErrorValidacion().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===== Configuración de listeners =====

    /**
     * Configura listeners de los controles de UI (botones, checkboxes, slider).
     */
    @SuppressLint("DefaultLocale")
    private void setupListeners() {
        setupDateListeners();
        setupSliderListener();
        setupCheckboxListeners();
        setupButtonListeners();
    }

    private void setupDateListeners() {
        binding.btnSelectDate.setOnClickListener(v -> abrirDatePicker(true));
        binding.btnSelectDateUntil.setOnClickListener(v -> abrirDatePicker(false));
    }

    private void setupSliderListener() {
        binding.rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() == 2) {
                float min = values.get(0);
                float max = values.get(1);

                binding.tvMinValue.setText(String.format("%.0f €", min));

                float maxImporte = viewModel.getMaxImporte();
                if (max >= maxImporte - 0.001f) {
                    binding.tvMaxValue.setText(String.format("%.2f €", max));
                } else {
                    binding.tvMaxValue.setText(String.format("%.0f €", max));
                }
            }
        });
    }

    private void setupCheckboxListeners() {
        binding.checkPagadas.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox("Pagada", isChecked));

        binding.checkPendientesPago.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox("Pendiente de pago", isChecked));

        binding.checkAnuladas.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox("Anulada", isChecked));

        binding.checkCuotaFija.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox("Cuota fija", isChecked));

        binding.checkPlanPago.setOnCheckedChangeListener((v, isChecked) ->
                actualizarEstadoCheckbox("Plan de pago", isChecked));
    }

    private void setupButtonListeners() {
        binding.btnAplicar.setOnClickListener(v -> aplicarFiltros());
        binding.btnBorrar.setOnClickListener(v -> viewModel.resetearFiltros());
        binding.btnCerrar.setOnClickListener(v -> cerrarFragmento());
    }

    // ===== Actualización de UI =====

    /**
     * Actualiza todos los controles de UI según el estado actual de los filtros.
     */
    @SuppressLint("DefaultLocale")
    private void actualizarUI(InvoiceFilters filtros) {
        if (filtros == null) return;

        actualizarFechas(filtros);
        actualizarSlider(filtros);
        actualizarCheckboxes(filtros);
    }

    /**
     * Actualiza los botones de fecha en la UI.
     * Si no hay fecha explícitamente seleccionada, muestra "día/mes/año" como placeholder.
     */
    private void actualizarFechas(InvoiceFilters filtros) {
        if (filtros.getFechaInicio() != null) {
            binding.btnSelectDate.setText(DateUtils.formatDateShort(filtros.getFechaInicio()));
        } else {
            binding.btnSelectDate.setText(R.string.dia_mes_ano);
        }

        if (filtros.getFechaFin() != null) {
            binding.btnSelectDateUntil.setText(DateUtils.formatDateShort(filtros.getFechaFin()));
        } else {
            binding.btnSelectDateUntil.setText(R.string.dia_mes_ano);
        }
    }

    @SuppressLint("DefaultLocale")
    private void actualizarSlider(InvoiceFilters filtros) {
        float maxImporte = viewModel.getMaxImporte();
        if (maxImporte > 0) {
            binding.rangeSlider.setValueFrom(0f);
            binding.rangeSlider.setValueTo(maxImporte);

            float minVal = Math.max(0f, filtros.getImporteMin().floatValue());
            float maxVal = Math.min(maxImporte, filtros.getImporteMax().floatValue());

            if (minVal > maxVal) minVal = maxVal;

            binding.rangeSlider.setValues(minVal, maxVal);
            binding.tvMaxImporte.setText(String.format("%.2f €", maxImporte));
            binding.tvMinValue.setText(String.format("%.0f €", minVal));
            binding.tvMaxValue.setText(String.format("%.0f €", maxVal));
        }
    }

    private void actualizarCheckboxes(InvoiceFilters filtros) {
        List<String> estados = filtros.getEstadosSeleccionados();
        if (estados == null) estados = new ArrayList<>();

        binding.checkPagadas.setChecked(estados.contains("Pagada"));
        binding.checkPendientesPago.setChecked(estados.contains("Pendiente de pago"));
        binding.checkAnuladas.setChecked(estados.contains("Anulada"));
        binding.checkCuotaFija.setChecked(estados.contains("Cuota fija"));
        binding.checkPlanPago.setChecked(estados.contains("Plan de pago"));
    }

    // ===== Gestión de filtros =====

    /**
     * Construye un objeto InvoiceFilters desde el estado actual de la UI.
     * Las fechas son tomadas directamente del ViewModel (null si no están seleccionadas).
     */
    private InvoiceFilters construirFiltrosDesdeUI() {
        InvoiceFilters filtros = new InvoiceFilters();

        // Recoger estados de los checkboxes
        List<String> estados = getStrings();
        filtros.setEstadosSeleccionados(estados);

        // Recoger fechas del ViewModel (pueden ser null si no están seleccionadas explícitamente)
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

    @NonNull
    private List<String> getStrings() {
        List<String> estados = new ArrayList<>();
        if (binding.checkPagadas.isChecked()) {
            estados.add(InvoiceState.PAGADA.getTextoServidor());
        }
        if (binding.checkPendientesPago.isChecked()) {
            estados.add(InvoiceState.PENDIENTE.getTextoServidor());
        }
        if (binding.checkAnuladas.isChecked()) {
            estados.add(InvoiceState.ANULADA.getTextoServidor());
        }
        if (binding.checkCuotaFija.isChecked()) {
            estados.add(InvoiceState.CUOTA_FIJA.getTextoServidor());
        }
        if (binding.checkPlanPago.isChecked()) {
            estados.add(InvoiceState.PLAN_PAGO.getTextoServidor());
        }
        return estados;
    }

    /**
     * Aplica los filtros construidos desde la UI.
     *
     * Estrategia de fechas:
     * - Si el usuario NO seleccionó fecha inicio → Usa la fecha más antigua del dataset
     * - Si el usuario NO seleccionó fecha fin → Usa la fecha más nueva del dataset
     * - Esto evita que aparezca el empty state cuando no hay selección explícita de fechas.
     * - Valida coherencia temporal antes de aplicar.
     */
    private void aplicarFiltros() {
        InvoiceFilters nuevosFiltros = construirFiltrosDesdeUI();

        // Determinar fechas efectivas para el filtrado interno
        LocalDate fechaInicioEfectiva = nuevosFiltros.getFechaInicio();
        if (fechaInicioEfectiva == null) {
            fechaInicioEfectiva = viewModel.getOldestDate();
        }

        LocalDate fechaFinEfectiva = nuevosFiltros.getFechaFin();
        if (fechaFinEfectiva == null) {
            fechaFinEfectiva = viewModel.getNewestDate();
            if (fechaFinEfectiva == null) fechaFinEfectiva = LocalDate.now();
        }

        // Validación de coherencia temporal
        if (fechaInicioEfectiva != null && fechaFinEfectiva != null &&
                fechaInicioEfectiva.isAfter(fechaFinEfectiva)) {
            Toast.makeText(requireContext(),
                    "La fecha de inicio no puede ser posterior a la final.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Aplicar las fechas efectivas al filtro antes de enviarlo al ViewModel
        nuevosFiltros.setFechaInicio(fechaInicioEfectiva);
        nuevosFiltros.setFechaFin(fechaFinEfectiva);

        viewModel.actualizarFiltros(nuevosFiltros);
        cerrarFragmento();
    }

    /**
     * Actualiza el estado de un checkbox específico en los filtros del ViewModel.
     * Mantiene sincronizados los valores del slider durante la operación.
     */
    private void actualizarEstadoCheckbox(String estado, boolean isChecked) {
        InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();
        if (filtros != null) {
            List<String> estados = new ArrayList<>(filtros.getEstadosSeleccionados());

            if (isChecked) {
                if (!estados.contains(estado)) estados.add(estado);
            } else {
                estados.remove(estado);
            }

            filtros.setEstadosSeleccionados(estados);

            // Preservar valores del slider para evitar resets visuales
            List<Float> currentSliderValues = binding.rangeSlider.getValues();
            if (currentSliderValues.size() >= 2) {
                filtros.setImporteMin((double) currentSliderValues.get(0));
                filtros.setImporteMax((double) currentSliderValues.get(1));
            }

            viewModel.actualizarEstadoFiltros(filtros);
        }
    }

    // ===== Date Picker =====

    /**
     * Abre un selector de fecha para inicio o fin.
     * Pre-selecciona inteligentemente según el contexto:
     * - Si ya había una fecha seleccionada → Abre en esa fecha
     * - Si no había fecha seleccionada → Abre en la fecha extrema apropiada (más antigua/más nueva)
     */
    private void abrirDatePicker(boolean esInicio) {
        InvoiceFilters filtrosActuales = viewModel.getFiltrosActuales().getValue();
        if (filtrosActuales == null) return;

        LocalDate fechaFiltro = esInicio
                ? filtrosActuales.getFechaInicio()
                : filtrosActuales.getFechaFin();

        long selection;
        if (fechaFiltro != null) {
            // Caso A: Ya había una fecha seleccionada por el usuario
            selection = fechaFiltro.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } else {
            // Caso B: No hay fecha seleccionada → Usar fecha inteligente del dataset
            LocalDate smartDate;
            if (esInicio) {
                smartDate = viewModel.getOldestDate();
            } else {
                smartDate = viewModel.getNewestDate();
                if (smartDate == null) smartDate = LocalDate.now();
            }

            if (smartDate != null) {
                selection = smartDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            } else {
                selection = MaterialDatePicker.todayInUtcMilliseconds();
            }
        }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(esInicio ? "Seleccionar Inicio" : "Seleccionar Fin")
                .setSelection(selection)
                .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedMillis -> {
            LocalDate nuevaFecha = Instant.ofEpochMilli(selectedMillis)
                    .atZone(ZoneOffset.UTC).toLocalDate();

            InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();
            if (filtros != null) {
                if (esInicio) {
                    filtros.setFechaInicio(nuevaFecha);
                } else {
                    filtros.setFechaFin(nuevaFecha);
                }
                viewModel.actualizarEstadoFiltros(filtros);
            }
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    // ===== Navegación =====

    /**
     * Cierra el fragmento de filtros y restaura la vista principal de la Activity.
     */
    private void cerrarFragmento() {
        if (getActivity() instanceof InvoiceListActivity) {
            ((InvoiceListActivity) getActivity()).restoreMainView();
        }
        getParentFragmentManager().popBackStack();
    }
}
