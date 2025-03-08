package es.studium.mispedidospendientes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.URI;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditarTiendaActivity extends AppCompatActivity {

    private TextView txtIdTienda;
    private EditText edtNombreTienda;
    private Button btnGuardar, btnVolver;
    private int idTienda;
    private static final String URL_BASE = "http://192.168.0.11/ApiRest/tiendas.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_tienda);

        // Inicializamos las vistas
        txtIdTienda = findViewById(R.id.txtIdTienda);
        edtNombreTienda = findViewById(R.id.edtNombreTienda);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVolver = findViewById(R.id.btnVolver);

        // Recuperamos los datos del Intent
        idTienda = getIntent().getIntExtra("idTienda", -1);
        String nombreTienda = getIntent().getStringExtra("nombreTienda");

        // Verificamos que el idTienda sea válido
        if (idTienda == -1) {
            Toast.makeText(this, "Error: ID de tienda no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar ID y nombre de la tienda en los campos correspondientes
        txtIdTienda.setText("ID: " + idTienda);
        edtNombreTienda.setText(nombreTienda);

        // Evento cuando se hace click en "Guardar"
        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = edtNombreTienda.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                // Llamamos al método de actualización de tienda
                actualizarTienda(idTienda, nuevoNombre);
            } else {
                Toast.makeText(this, "El nombre de la tienda no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        // Evento cuando se hace click en "Volver"
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(EditarTiendaActivity.this, TiendaActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void actualizarTienda(int idTienda, String nombreTienda) {
        OkHttpClient client = new OkHttpClient();

        // Construimos la URL con los parámetros de la query
        HttpUrl.Builder queryUrlBuilder = HttpUrl.get(URI.create(URL_BASE)).newBuilder();
        queryUrlBuilder.addQueryParameter("idTienda", String.valueOf(idTienda));
        queryUrlBuilder.addQueryParameter("nombreTienda", nombreTienda);

        // Body vacío (requerido para PUT en algunos servidores)
        RequestBody formBody = new FormBody.Builder().build();

        // Log para verificar el cuerpo de la solicitud
        Log.i("ActualizarTienda", "RequestBody: " + formBody.toString());

        // Construimos la solicitud PUT
        Request request = new Request.Builder()
                .url(queryUrlBuilder.build())
                .put(formBody)
                .build();

        // Log para verificar la solicitud
        Log.i("ActualizarTienda", "Request: " + request.toString());

        // Realizamos la llamada asíncrona a la API
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // En caso de error, mostramos un mensaje en la UI
                runOnUiThread(() -> {
                    Toast.makeText(EditarTiendaActivity.this, "Error al actualizar la tienda", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Si la respuesta es exitosa, mostramos el mensaje correspondiente
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditarTiendaActivity.this, "Tienda actualizada con éxito", Toast.LENGTH_SHORT).show();

                        // Mandamos el Intent para notificar a TiendaActivity que recargue las tiendas
                        Intent intent = new Intent(EditarTiendaActivity.this, TiendaActivity.class);
                        intent.putExtra("actualizar_lista", true); // Indicamos que debe actualizarse
                        startActivity(intent);
                        finish(); // Terminamos la actividad de edición
                    } else {
                        Toast.makeText(EditarTiendaActivity.this, "Error al actualizar la tienda", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
