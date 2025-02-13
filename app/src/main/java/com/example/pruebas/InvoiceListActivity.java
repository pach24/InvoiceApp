package com.example.pruebas;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pruebas.databinding.ActivityInvoiceListBinding;
import com.example.pruebas.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class InvoiceListActivity extends AppCompatActivity {

    private ActivityInvoiceListBinding bindingInvoiceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingInvoiceList = bindingInvoiceList.inflate(getLayoutInflater());
        View view = bindingInvoiceList.getRoot();
        setContentView(view);

        ListView listViewNumbers = findViewById(R.id.listViewNumbers);
        Button btIrMain = findViewById(R.id.btIrMain);

        ArrayList<String> numbers = new ArrayList<>();
        numbers.add("Número 1");
        numbers.add("Número 2");
        numbers.add("Número 3");

        // Configura el adaptador para el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, numbers);
        listViewNumbers.setAdapter(adapter);

        // Configura el botón para regresar a la actividad anterior
        btIrMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra esta actividad y vuelve a la anterior
            }
        });
    }
}
