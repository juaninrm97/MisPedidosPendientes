package es.studium.mispedidospendientes;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;
import okhttp3.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NuevoPedidoActivity extends AppCompatActivity {

    private EditText edtFechaPedido, edtDescripcionPedido, edtImportePedido;
    private Spinner spinnerTiendas;
    private Button btnGuardarPedido, btnVolver; // Añadido el botón Volver
    private List<Tienda> listaTiendas;
    private int idTiendaSeleccionada = -1; // Para almacenar la tienda elegida

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevo_pedido);

        edtFechaPedido = findViewById(R.id.edtFechaPedido);
        edtDescripcionPedido = findViewById(R.id.edtDescripcionPedido);
        edtImportePedido = findViewById(R.id.edtImportePedido);
        spinnerTiendas = findViewById(R.id.spinnerTiendas);
        btnGuardarPedido = findViewById(R.id.btnGuardarPedido);
        btnVolver = findViewById(R.id.btnVolver); // Obtener el botón Volver

        // Configurar el botón Volver
        btnVolver.setOnClickListener(v -> {
            // Volver a MainActivity
            Intent intent = new Intent(NuevoPedidoActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Asegura que MainActivity se recargue
            startActivity(intent);
            finish(); // Finaliza esta actividad para que no quede en el back stack
        });

        // Cargar tiendas en el spinner
        cargarTiendas();

        // Configurar el DatePickerDialog para la fecha
        edtFechaPedido.setOnClickListener(v -> mostrarDatePicker());

        // Acción para guardar el pedido
        btnGuardarPedido.setOnClickListener(v -> guardarPedido());
    }

    private void cargarTiendas() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/tiendas.php") // Asegúrate de que la IP sea correcta
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(NuevoPedidoActivity.this, "Error de red al cargar tiendas", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    listaTiendas = new Gson().fromJson(jsonResponse, new TypeToken<List<Tienda>>() {}.getType());

                    // Ordenar la lista de tiendas alfabéticamente por el nombre
                    Collections.sort(listaTiendas, new Comparator<Tienda>() {
                        @Override
                        public int compare(Tienda t1, Tienda t2) {
                            return t1.getNombreTienda().compareToIgnoreCase(t2.getNombreTienda());
                        }
                    });

                    // Convertir lista de tiendas en nombres para el Spinner
                    List<String> nombresTiendas = new ArrayList<>();
                    for (Tienda tienda : listaTiendas) {
                        nombresTiendas.add(tienda.getNombreTienda());
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(NuevoPedidoActivity.this, android.R.layout.simple_spinner_item, nombresTiendas);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerTiendas.setAdapter(adapter);
                    });

                    // Manejar selección de tienda
                    spinnerTiendas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            idTiendaSeleccionada = listaTiendas.get(position).getIdTienda(); // Guardar ID de la tienda seleccionada
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            idTiendaSeleccionada = -1; // Si no selecciona nada
                        }
                    });
                }
            }
        });
    }


    private void mostrarDatePicker() {
        // Crear un Calendar para obtener la fecha actual
        Calendar calendar = Calendar.getInstance();

        // Crear el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                NuevoPedidoActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    // Crear un objeto Calendar con la fecha seleccionada
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, monthOfYear, dayOfMonth);

                    // Usar SimpleDateFormat para formatear la fecha en formato europeo
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String fechaFormateada = sdf.format(selectedDate.getTime());

                    // Mostrar la fecha formateada en el EditText
                    edtFechaPedido.setText(fechaFormateada);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void guardarPedido() {
        String fechaPedido = edtFechaPedido.getText().toString();
        String descripcionPedido = edtDescripcionPedido.getText().toString();
        String importePedido = edtImportePedido.getText().toString();

        if (fechaPedido.isEmpty() || descripcionPedido.isEmpty() || importePedido.isEmpty() || idTiendaSeleccionada == -1) {
            Toast.makeText(NuevoPedidoActivity.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir la fecha del formato europeo (dd/MM/yyyy) a formato americano (yyyy-MM-dd)
        SimpleDateFormat formatoEuropeo = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat formatoAmericano = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            // Parsear la fecha en formato europeo
            java.util.Date date = formatoEuropeo.parse(fechaPedido);
            // Convertir la fecha al formato americano
            String fechaPedidoAmericana = formatoAmericano.format(date);

            // Ahora ya tienes la fecha en formato americano
            Log.d("NuevoPedido", "Fecha convertida: " + fechaPedidoAmericana); // Para depuración

            // Crear la solicitud POST con la fecha convertida
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("fechaEstimadaPedido", fechaPedidoAmericana)  // Fecha convertida a formato americano
                    .add("descripcionPedido", descripcionPedido)
                    .add("importePedido", importePedido)
                    .add("idTiendaFK", String.valueOf(idTiendaSeleccionada)) // Enviar la tienda seleccionada
                    .build();

            Request request = new Request.Builder()
                    .url("http://192.168.0.11/ApiRest/pedidos.php") // URL corregida
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(NuevoPedidoActivity.this, "Error al guardar el pedido", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(NuevoPedidoActivity.this, "Pedido guardado con éxito", Toast.LENGTH_SHORT).show();
                            // Regresar a la MainActivity para actualizar la lista
                            Intent intent = new Intent(NuevoPedidoActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Asegura que MainActivity se recargue
                            startActivity(intent);
                            finish(); // Cierra la actividad actual
                        });
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(NuevoPedidoActivity.this, "Error con el formato de la fecha", Toast.LENGTH_SHORT).show();
        }
    }
}
