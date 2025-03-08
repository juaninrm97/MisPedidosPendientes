package es.studium.mispedidospendientes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Pedido> pedidos;
    private List<Tienda> listaTiendas;
    private Button btnNuevoPedido, btnVerTiendas;
    private PedidoAdapter pedidoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewPedidos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnNuevoPedido = findViewById(R.id.btnNuevoPedido);
        btnVerTiendas = findViewById(R.id.btnTienda);

        // Asignar eventos a los botones
        btnNuevoPedido.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NuevoPedidoActivity.class);
            startActivity(intent);
        });

        btnVerTiendas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TiendaActivity.class);
            startActivity(intent);
        });

        // Cargar pedidos y tiendas al inicio
        cargarPedidos();
    }

    private void cargarPedidos() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/pedidos.php") // Asegúrate de que la IP sea correcta
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error de red al cargar pedidos", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    pedidos = new Gson().fromJson(jsonResponse, new TypeToken<List<Pedido>>() {}.getType());

                    // Formatear la fecha de cada pedido a formato europeo
                    for (Pedido pedido : pedidos) {
                        String fechaFormateada = formatearFecha(pedido.getFechaEstimada());
                        pedido.setFechaEstimada(fechaFormateada);  // Actualizamos la fecha formateada en el pedido
                    }

                    // Cargar tiendas
                    cargarTiendas();
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error al cargar los pedidos desde la API", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void cargarTiendas() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/tiendas.php") // Asegúrate de que la IP sea correcta
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error de red al cargar tiendas", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonResponse = response.body().string();
                    listaTiendas = new Gson().fromJson(jsonResponse, new TypeToken<List<Tienda>>() {}.getType());

                    // Ahora inicializa el adapter con la lista de pedidos y tiendas
                    runOnUiThread(() -> {
                        pedidoAdapter = new PedidoAdapter(pedidos, MainActivity.this, listaTiendas);
                        recyclerView.setAdapter(pedidoAdapter);
                    });
                }
            }
        });
    }

    // Este es el método que se requiere en el TiendaAdapter para actualizar la lista de pedidos
    public void actualizarPedidos() {
        cargarPedidos();  // Vuelve a cargar los pedidos desde la API
        Toast.makeText(this, "Pedidos actualizados", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            int idPedido = data.getIntExtra("idPedido", -1);
            String descripcionPedido = data.getStringExtra("descripcionPedido");
            double importePedido = data.getDoubleExtra("importePedido", 0);
            int estadoPedido = data.getIntExtra("estadoPedido", 0);  // Cambiado de boolean a int (0 o 1)

            // Verificar que el idPedido sea válido
            if (idPedido != -1) {
                // Si el pedido está entregado, lo eliminamos de la lista
                if (estadoPedido == 1) {
                    for (int i = 0; i < pedidos.size(); i++) {
                        if (pedidos.get(i).getIdPedido() == idPedido) {
                            pedidos.remove(i);
                            pedidoAdapter.notifyItemRemoved(i);  // Elimina el pedido del RecyclerView
                            break;
                        }
                    }
                } else {
                    // Si solo se ha actualizado (no entregado), actualizamos los datos
                    for (int i = 0; i < pedidos.size(); i++) {
                        if (pedidos.get(i).getIdPedido() == idPedido) {
                            pedidos.get(i).setDescripcionPedido(descripcionPedido);
                            pedidos.get(i).setImportePedido(importePedido);
                            pedidos.get(i).setEstadoPedido(estadoPedido); // Actualiza el estado
                            pedidoAdapter.notifyItemChanged(i); // Actualiza el item del RecyclerView
                            break;
                        }
                    }
                }

                // Forzar la actualización de toda la lista en caso de que haya sido modificada la lista completa
                pedidoAdapter.notifyDataSetChanged();

                // Mostrar un mensaje de éxito
                Toast.makeText(MainActivity.this, "Pedido actualizado correctamente", Toast.LENGTH_SHORT).show();
            }
        }
    }




    // Método para formatear las fechas de formato americano (yyyy-MM-dd) a europeo (dd/MM/yyyy)
    public String formatearFecha(String fechaOriginal) {
        try {
            // Suponiendo que la fecha recibida de la API está en formato "yyyy-MM-dd" (americano)
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = originalFormat.parse(fechaOriginal);

            // Convertimos a formato europeo (dd/MM/yyyy)
            SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return newFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal; // Si hay error, retornamos la fecha original
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar los pedidos si es necesario
        cargarPedidos();
    }
}
