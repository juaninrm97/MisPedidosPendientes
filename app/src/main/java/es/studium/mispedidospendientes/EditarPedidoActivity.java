package es.studium.mispedidospendientes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
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

public class EditarPedidoActivity extends AppCompatActivity {

    private EditText edtDescripcionPedido, edtImportePedido;
    private CheckBox chkEntregado;
    private Button btnGuardar, btnVolver;
    private int idPedido;
    private static final String URL_BASE = "http://192.168.0.11/ApiRest/pedidos.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_pedido);

        // Inicializamos las vistas
        edtDescripcionPedido = findViewById(R.id.edtDescripcionPedido);
        edtImportePedido = findViewById(R.id.edtImportePedido);
        chkEntregado = findViewById(R.id.chkEntregado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVolver = findViewById(R.id.btnVolver);

        // Recuperamos los datos del Intent
        idPedido = getIntent().getIntExtra("idPedido", -1);
        String descripcionPedido = getIntent().getStringExtra("descripcionPedido");
        double importePedido = getIntent().getDoubleExtra("importePedido", 0);
        int estadoPedido = getIntent().getIntExtra("estadoPedido", 0);  // Cambiado a int

        // Verificamos que el idPedido sea válido
        if (idPedido == -1) {
            Toast.makeText(this, "Error: ID de pedido no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Rellenamos los campos con los datos recibidos
        edtDescripcionPedido.setText(descripcionPedido);

        // Formateamos el importe
        edtImportePedido.setText(String.format("%.0f€", importePedido));

        // Marcamos el CheckBox de entregado si el estado es 1
        chkEntregado.setChecked(estadoPedido == 1);

        // Evento cuando se hace click en "Guardar"
        btnGuardar.setOnClickListener(v -> {
            String nuevaDescripcion = edtDescripcionPedido.getText().toString().trim();
            String nuevoImporteStr = edtImportePedido.getText().toString().trim();
            int entregado = chkEntregado.isChecked() ? 1 : 0;  // Convertimos a 1 o 0

            if (!nuevaDescripcion.isEmpty() && !nuevoImporteStr.isEmpty()) {
                // Eliminar el símbolo de euro y convertir a número
                nuevoImporteStr = nuevoImporteStr.replace("€", "").trim();  // Eliminamos el símbolo €
                double nuevoImporte = Double.parseDouble(nuevoImporteStr);  // Convertimos a número

                // Llamamos al método para actualizar el pedido
                actualizarPedido(idPedido, nuevaDescripcion, String.valueOf(nuevoImporte), entregado);
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // Evento cuando se hace click en "Volver"
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(EditarPedidoActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void actualizarPedido(int idPedido, String nuevaDescripcion, String nuevoImporte, int entregado) {
        OkHttpClient client = new OkHttpClient();

        // Construir la URL con los parámetros de la query
        HttpUrl.Builder queryUrlBuilder = HttpUrl.get(URI.create(URL_BASE)).newBuilder();
        queryUrlBuilder.addQueryParameter("idPedido", String.valueOf(idPedido));
        queryUrlBuilder.addQueryParameter("fechaPedido", "2025-03-08");
        queryUrlBuilder.addQueryParameter("fechaEstimadaPedido", "2025-03-13");
        queryUrlBuilder.addQueryParameter("descripcionPedido", nuevaDescripcion);
        queryUrlBuilder.addQueryParameter("importePedido", nuevoImporte);
        queryUrlBuilder.addQueryParameter("estadoPedido", String.valueOf(entregado));  // Estado como 0 o 1
        queryUrlBuilder.addQueryParameter("idTiendaFK", "22");

        // Body vacío (requerido para PUT en algunos servidores)
        RequestBody formBody = new FormBody.Builder().build();

        // Log para verificar el cuerpo de la solicitud
        Log.i("ActualizarPedido", "RequestBody: " + formBody.toString());

        // Crear la solicitud PUT
        Request request = new Request.Builder()
                .url(queryUrlBuilder.build())
                .put(formBody)  // El cuerpo de la solicitud sigue siendo vacío
                .build();

        // Realizar la llamada asíncrona a la API
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // En caso de error, mostrar mensaje en la UI
                runOnUiThread(() -> {
                    Toast.makeText(EditarPedidoActivity.this, "Error al actualizar el pedido", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Si la respuesta es exitosa, mostrar mensaje en la UI
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditarPedidoActivity.this, "Pedido actualizado con éxito", Toast.LENGTH_SHORT).show();

                        // Notificar al MainActivity para que recargue los datos
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("idPedido", idPedido);
                        resultIntent.putExtra("descripcionPedido", nuevaDescripcion);
                        resultIntent.putExtra("importePedido", nuevoImporte);
                        resultIntent.putExtra("estadoPedido", entregado);  // Enviamos el estado como int
                        setResult(RESULT_OK, resultIntent);
                        finish();  // Terminamos la actividad
                    } else {
                        Toast.makeText(EditarPedidoActivity.this, "Error al actualizar el pedido", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
