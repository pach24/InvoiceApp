package com.example.pruebas;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.slider.RangeSlider;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.pruebas.databinding.FragmentFilterBinding;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FilterFragment extends Fragment {
    private FragmentFilterBinding binding;
    private InvoiceViewModel viewModel;

    public FilterFragment() { // Constructor vacío
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilterBinding.inflate(inflater, container, false);

        // Obtener el ViewModel de Factura
        viewModel = new ViewModelProvider(requireActivity()).get(InvoiceViewModel.class);
        binding.rangeSlider.setValues(10f, 90f);

        // Valores por defecto de las fechas
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fechaDefault = dateFormat.format(Calendar.getInstance().getTime());
        binding.btnSelectDate.setText(fechaDefault);
        binding.btnSelectDateUntil.setText(fechaDefault);

        // Configurar el DatePickerDialog para fecha desde
        binding.btnSelectDate.setOnClickListener(v -> openDatePicker(binding.btnSelectDate));

        // Configurar el DatePickerDialog para fecha hasta
        binding.btnSelectDateUntil.setOnClickListener(v -> openDatePicker(binding.btnSelectDateUntil));

        // Recuperar el Bundle con maxImporte
        Bundle bundle = getArguments();
        if (bundle != null) {
            float maxImporte = bundle.getFloat("MAX_IMPORTE", 0f);
            if (maxImporte > 0) {
                binding.rangeSlider.setValueFrom(0f);
                binding.rangeSlider.setValueTo(maxImporte);
                binding.rangeSlider.setValues(0f, maxImporte);
                binding.tvMinValue.setText("0 €");
                binding.tvMaxValue.setText(maxImporte + " €");
                binding.tvMaxImporte.setText(maxImporte + " €");
            } else {
                binding.tvMinValue.setText("0 €");
                binding.tvMaxValue.setText("0 €");
            }
        }

        // Listener para el RangeSlider
        binding.rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() == 2) {
                float minValue = values.get(0);
                float maxValue = values.get(1);
                binding.tvMinValue.setText(String.format("%.0f €", minValue));
                binding.tvMaxValue.setText(String.format("%.0f €", maxValue));
            }
        });

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Botón aplicar filtros
        binding.btnAplicar.setOnClickListener(v -> {
            List<String> estados = getStrings();
            String fechaInicio = binding.btnSelectDate.getText().toString();
            String fechaFin = binding.btnSelectDateUntil.getText().toString();

            List<Float> valoresSlider = binding.rangeSlider.getValues();
            float importeMin = valoresSlider.get(0);
            float importeMax = valoresSlider.get(1);

            // Crear un Bundle para pasar los datos
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("ESTADOS", new ArrayList<>(estados));
            bundle.putString("FECHA_INICIO", fechaInicio);
            bundle.putString("FECHA_FIN", fechaFin);
            bundle.putDouble("IMPORTE_MIN", importeMin);
            bundle.putDouble("IMPORTE_MAX", importeMax);

            // Pasar los datos a la actividad
            InvoiceListActivity activity = (InvoiceListActivity) getActivity();
            if (activity != null) {
                activity.aplicarFiltros(bundle); // Llama al método para aplicar los filtros
                activity.restoreMainView();
            }

            // Cerrar el fragmento
            getParentFragmentManager().popBackStack();
        });

        // Botón cerrar fragmento filtros
        binding.btnCerrar.setOnClickListener(v -> {
            if (getActivity() != null) {
                InvoiceListActivity activity = (InvoiceListActivity) getActivity();
                activity.restoreMainView();
            }
            getParentFragmentManager().popBackStack();
        });

        // Botón borrar filtros
        binding.btnBorrar.setOnClickListener(v -> {
            resetFilters();
        });
    }

    private void openDatePicker(View button) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                (view, year1, monthOfYear, dayOfMonth1) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth1);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selectedDate.getTime());
                    ((Button) button).setText(formattedDate);
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void resetFilters() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fechaDefault = dateFormat.format(Calendar.getInstance().getTime());
        binding.btnSelectDate.setText(fechaDefault);
        binding.btnSelectDateUntil.setText(fechaDefault);
        binding.rangeSlider.setValues(0f, viewModel.getMaxImporte()); // Si tienes esta función en InvoiceResponse
        binding.checkPagadas.setChecked(false);
        binding.checkAnuladas.setChecked(false);
        binding.checkCuotaFija.setChecked(false);
        binding.checkPendientesPago.setChecked(false);
        binding.checkPlanPago.setChecked(false);
    }

    @NonNull
    private List<String> getStrings() { // Estados
        List<String> estados = new ArrayList<>();
        if (binding.checkPagadas.isChecked()) {
            estados.add("Pagada");
        }
        if (binding.checkPendientesPago.isChecked()) {
            estados.add("Pendiente de pago");
        }
        if (binding.checkCuotaFija.isChecked()) {
            estados.add("Cuota Fija");
        }
        if (binding.checkPlanPago.isChecked()) {
            estados.add("Plan de pago");
        }
        if (binding.checkAnuladas.isChecked()) {
            estados.add("Anulada");
        }
        return estados;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
