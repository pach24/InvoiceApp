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

import com.nexosolar.android.R;
import com.nexosolar.android.core.DateUtils;
import com.nexosolar.android.databinding.FragmentFilterBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.nexosolar.android.domain.models.InvoiceFilters;
import com.nexosolar.android.domain.models.InvoiceState;

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

        viewModel.resetearFiltros();

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
     * Actualiza la UI según el estado de los filtros en el ViewModel.
     */
    @SuppressLint("DefaultLocale")
    private void actualizarUI(InvoiceFilters filtros) {
        if (filtros == null) return;

        // 1. ACTUALIZAR TEXTOS DE FECHAS
        // Si hay fecha -> Formato corto (25/01/24)
        // Si NO hay fecha -> Texto por defecto (día/mes/año)
        if (filtros.getFechaInicio() != null) {
            binding.btnSelectDate.setText(DateUtils.formatDateShort(filtros.getFechaInicio()));
        } else {
            binding.btnSelectDate.setText(R.string.dia_mes_ano); // <--- IMPORTANTE
        }

        if (filtros.getFechaFin() != null) {
            binding.btnSelectDateUntil.setText(DateUtils.formatDateShort(filtros.getFechaFin()));
        } else {
            binding.btnSelectDateUntil.setText(R.string.dia_mes_ano); // <--- IMPORTANTE
        }

        // 2. ACTUALIZAR SLIDER DE IMPORTE
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

        // 3. ACTUALIZAR CHECKBOXES
        List<String> estados = filtros.getEstadosSeleccionados();
        if (estados == null) estados = new ArrayList<>();

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

        // 1. Listeners de Fechas (Date Pickers)
        binding.btnSelectDate.setOnClickListener(v -> abrirDatePicker(true));
        binding.btnSelectDateUntil.setOnClickListener(v -> abrirDatePicker(false));

        // 2. Listener del Slider (SOLO lógica del slider)
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
        }); // <--- AQUÍ SE CIERRA EL LISTENER DEL SLIDER

        // 3. Listeners de Checkboxes (INDEPENDIENTES)
        binding.checkPagadas.setOnCheckedChangeListener((v, isChecked) -> actualizarEstadoCheckbox("Pagada", isChecked));
        binding.checkPendientesPago.setOnCheckedChangeListener((v, isChecked) -> actualizarEstadoCheckbox("Pendiente de pago", isChecked));
        binding.checkAnuladas.setOnCheckedChangeListener((v, isChecked) -> actualizarEstadoCheckbox("Anulada", isChecked));
        binding.checkCuotaFija.setOnCheckedChangeListener((v, isChecked) -> actualizarEstadoCheckbox("Cuota fija", isChecked));
        binding.checkPlanPago.setOnCheckedChangeListener((v, isChecked) -> actualizarEstadoCheckbox("Plan de pago", isChecked));

        // 4. Listener del Botón Aplicar (INDEPENDIENTE)
        binding.btnAplicar.setOnClickListener(v -> {
            InvoiceFilters nuevosFiltros = construirFiltrosDesdeUI();

            // 1. Obtener fechas efectivas
            LocalDate fechaInicioEfectiva = nuevosFiltros.getFechaInicio();
            if (fechaInicioEfectiva == null) fechaInicioEfectiva = viewModel.getOldestDate();

            LocalDate fechaFinEfectiva = nuevosFiltros.getFechaFin();
            if (fechaFinEfectiva == null) {
                fechaFinEfectiva = viewModel.getNewestDate();
                if (fechaFinEfectiva == null) fechaFinEfectiva = LocalDate.now();
            }

            // 2. Validación
            if (fechaInicioEfectiva != null && fechaFinEfectiva != null &&
                    fechaInicioEfectiva.isAfter(fechaFinEfectiva)) {

                Toast.makeText(requireContext(),
                        "La fecha de inicio no puede ser posterior a la final.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // 3. Aplicar
            viewModel.actualizarFiltros(nuevosFiltros);
            cerrarFragmento();
        });

        // 5. Botones Borrar y Cerrar
        binding.btnBorrar.setOnClickListener(v -> viewModel.resetearFiltros());
        binding.btnCerrar.setOnClickListener(v -> cerrarFragmento());
    }



    /**
     * Construye un objeto InvoiceFilters desde el estado actual de la UI
     */
    private InvoiceFilters construirFiltrosDesdeUI() {
        InvoiceFilters filtros = new InvoiceFilters();

        // Recoger estados de los checkboxes
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

        filtros.setEstadosSeleccionados(estados);
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

    private void actualizarEstadoCheckbox(String estado, boolean isChecked) {
        InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();

        if (filtros != null) {
            // 1. Gestionar la lista de estados (añadir o quitar)
            List<String> estados = new ArrayList<>(filtros.getEstadosSeleccionados());
            if (isChecked) {
                if (!estados.contains(estado)) estados.add(estado);
            } else {
                estados.remove(estado);
            }
            filtros.setEstadosSeleccionados(estados);

            // 2. IMPORTANTE: Guardar también la posición actual del slider
            // Si no hacemos esto, al notificarse el cambio al ViewModel, este volverá a llamar a actualizarUI()
            // con los valores antiguos de importe, reseteando el slider visualmente.
            List<Float> currentSliderValues = binding.rangeSlider.getValues();
            if (currentSliderValues.size() >= 2) {
                filtros.setImporteMin((double) currentSliderValues.get(0));
                filtros.setImporteMax((double) currentSliderValues.get(1));
            }

            // 3. Notificar al ViewModel con el estado completo actualizado
            viewModel.actualizarEstadoFiltros(filtros);
        }
    }

    /**
     * Abre el date picker para seleccionar fecha de inicio o fin
     */

    private void abrirDatePicker(boolean esInicio) {
        InvoiceFilters filtrosActuales = viewModel.getFiltrosActuales().getValue();
        if (filtrosActuales == null) return;

        LocalDate fechaFiltro = esInicio ? filtrosActuales.getFechaInicio() : filtrosActuales.getFechaFin();
        long selection;

        if (fechaFiltro != null) {
            // Caso A: El usuario ya había elegido una fecha -> Abrimos ahí
            selection = fechaFiltro.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        } else {
            // Caso B: Texto "día/mes/año" -> Calculamos fecha inteligente
            LocalDate smartDate;
            if (esInicio) {
                // Para fecha INICIO -> Usamos la fecha de la PRIMERA factura
                smartDate = viewModel.getOldestDate();
            } else {
                // Para fecha FIN -> Usamos la fecha de la ÚLTIMA factura (o Hoy)
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
                .setSelection(selection) // Aquí aplicamos la lógica inteligente
                .setTheme(com.google.android.material.R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedMillis -> {
            LocalDate nuevaFecha = Instant.ofEpochMilli(selectedMillis)
                    .atZone(ZoneOffset.UTC).toLocalDate(); // <--- Esta es la fecha elegida por el usuario

            InvoiceFilters filtros = viewModel.getFiltrosActuales().getValue();
            if (filtros != null) {
                if (esInicio) {
                    filtros.setFechaInicio(nuevaFecha); // <--- Asignamos la elegida
                } else {
                    filtros.setFechaFin(nuevaFecha);
                }
                viewModel.actualizarEstadoFiltros(filtros); // Notificamos
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
