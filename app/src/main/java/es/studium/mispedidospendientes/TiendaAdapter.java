package es.studium.mispedidospendientes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TiendaAdapter extends RecyclerView.Adapter<TiendaAdapter.TiendaViewHolder> {

    private List<Tienda> tiendaList;

    public TiendaAdapter(List<Tienda> tiendaList) {
        this.tiendaList = tiendaList;
    }

    @Override
    public TiendaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tienda, parent, false); // Asegúrate de tener un layout adecuado para cada item
        return new TiendaViewHolder(itemView);
    }

    private void eliminarTienda(int idTienda, Context context) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/tiendas.php?idTienda=" + idTienda)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Tienda eliminada", Toast.LENGTH_SHORT).show();

                        // Eliminar los pedidos asociados a la tienda eliminada
                        eliminarPedidosPorTienda(idTienda, context);

                        // Actualizar la vista
                        ((TiendaActivity) context).cargarTiendas(); // Recargar lista de tiendas
                        // Aquí puedes también llamar a tu RecyclerView para actualizar la lista de pedidos
                        ((MainActivity) context).actualizarPedidos(); // Método para actualizar pedidos
                    });
                }
            }
        });
    }

    private void eliminarPedidosPorTienda(int idTienda, Context context) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://192.168.0.11/ApiRest/pedidos.php?tiendaId=" + idTienda)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Error al eliminar los pedidos de la tienda", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Pedidos de la tienda eliminados", Toast.LENGTH_SHORT).show();
                        // Aquí puedes también actualizar la lista de pedidos en la UI
                        ((MainActivity) context).actualizarPedidos();
                    });
                }
            }
        });
    }



    @Override
    public void onBindViewHolder(TiendaViewHolder holder, int position) {
        Tienda tienda = tiendaList.get(position);
        holder.nombreTienda.setText(tienda.getNombreTienda());

        // Click para editar
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditarTiendaActivity.class);
            intent.putExtra("idTienda", tienda.getIdTienda());
            intent.putExtra("nombreTienda", tienda.getNombreTienda());
            v.getContext().startActivity(intent);
        });

        // Mantener presionado para eliminar
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Tienda")
                    .setMessage("¿Seguro que deseas eliminar esta tienda?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminarTienda(tienda.getIdTienda(), v.getContext()))
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true; // Indica que el evento está manejado
        });
    }


    @Override
    public int getItemCount() {
        return tiendaList != null ? tiendaList.size() : 0;
    }

    public class TiendaViewHolder extends RecyclerView.ViewHolder {
        public TextView nombreTienda;

        public TiendaViewHolder(View view) {
            super(view);
            nombreTienda = view.findViewById(R.id.nombreTienda); // Asegúrate de que tienes el ID correcto en el layout
        }
    }
}
