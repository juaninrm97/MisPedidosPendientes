package es.studium.mispedidospendientes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class NuevaTiendaActivity extends AppCompatActivity {

    private EditText edtNombreTienda;
    private Button btnGuardarTienda, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_tienda);

        edtNombreTienda = findViewById(R.id.edtNombreTienda);
        btnGuardarTienda = findViewById(R.id.btnGuardarTienda);
        btnVolver = findViewById(R.id.btnVolver);  // Encuentra el botón "Volver"

        // Configurar el botón "Guardar Tienda"
        btnGuardarTienda.setOnClickListener(v -> guardarTienda());

        // Configurar el botón "Volver"
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(NuevaTiendaActivity.this, TiendaActivity.class);
            startActivity(intent);  // Regresar a TiendaActivity
            finish();  // Finalizar la actividad actual
        });
    }

    private void guardarTienda() {
        String nombreTienda = edtNombreTienda.getText().toString().trim();

        if (nombreTienda.isEmpty()) {
            Toast.makeText(this, "El nombre de la tienda no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("nombreTienda", nombreTienda)
                .build();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/tiendas.php") // Verifica que la URL sea correcta
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(NuevaTiendaActivity.this, "Error al guardar la tienda", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(NuevaTiendaActivity.this, "Tienda guardada con éxito", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(NuevaTiendaActivity.this, TiendaActivity.class);
                        intent.putExtra("actualizar", true); // Enviar una señal para actualizar la lista
                        startActivity(intent); // Regresar a TiendaActivity
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(NuevaTiendaActivity.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
