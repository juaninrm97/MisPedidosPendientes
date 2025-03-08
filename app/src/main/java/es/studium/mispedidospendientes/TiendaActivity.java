package es.studium.mispedidospendientes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class TiendaActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTiendas;
    private TiendaAdapter tiendaAdapter;
    private Button btnNuevaTienda, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tienda);

        // Inicializar RecyclerView
        recyclerViewTiendas = findViewById(R.id.recyclerViewTiendas);
        recyclerViewTiendas.setLayoutManager(new LinearLayoutManager(this));

        // Configurar botón para nueva tienda
        btnNuevaTienda = findViewById(R.id.btnNuevaTienda);
        btnNuevaTienda.setOnClickListener(v -> {
            // Abrir actividad para agregar nueva tienda
            Intent intent = new Intent(TiendaActivity.this, NuevaTiendaActivity.class);
            startActivity(intent);
        });

        // Configurar botón Volver
        btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            // Volver a la actividad MainActivity
            Intent intent = new Intent(TiendaActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finaliza TiendaActivity para que no quede en el back stack
        });

        // Verifica si se ha recibido el extra "actualizar_lista"
        if (getIntent().getBooleanExtra("actualizar_lista", false)) {
            cargarTiendas();  // Si es así, recarga las tiendas
        } else {
            // Cargar datos desde la API normalmente
            cargarTiendas();
        }
    }

    public void cargarTiendas() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/tiendas.php")  // URL de la API
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Muestra un mensaje de error de red
                runOnUiThread(() -> {
                    Toast.makeText(TiendaActivity.this, "Error de red: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                Log.e("TiendaActivity", "Error de red", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();

                    // Verifica que los datos JSON no estén vacíos
                    if (jsonResponse != null && !jsonResponse.isEmpty()) {
                        Log.d("API_RESPONSE", "Respuesta de la API: " + jsonResponse);

                        // Deserializa la respuesta JSON a una lista de objetos Tienda
                        List<Tienda> tiendas = new Gson().fromJson(jsonResponse, new TypeToken<List<Tienda>>() {}.getType());

                        // Verifica que la lista de tiendas no esté vacía
                        if (tiendas != null && !tiendas.isEmpty()) {
                            // Ordenar la lista de tiendas alfabéticamente por nombre
                            Collections.sort(tiendas, new Comparator<Tienda>() {
                                @Override
                                public int compare(Tienda t1, Tienda t2) {
                                    return t1.getNombreTienda().compareToIgnoreCase(t2.getNombreTienda());
                                }
                            });

                            runOnUiThread(() -> {
                                tiendaAdapter = new TiendaAdapter(tiendas);  // Asegúrate de asignar el Adapter
                                recyclerViewTiendas.setAdapter(tiendaAdapter);  // Asociar el Adapter al RecyclerView
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(TiendaActivity.this, "No se encontraron tiendas", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(TiendaActivity.this, "Respuesta vacía de la API", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(TiendaActivity.this, "Error en la respuesta de la API. Código: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                    Log.e("TiendaActivity", "Error en la respuesta de la API: Código " + response.code());
                }
            }
        });
    }
}
