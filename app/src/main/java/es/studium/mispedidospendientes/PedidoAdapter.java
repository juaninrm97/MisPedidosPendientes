package es.studium.mispedidospendientes;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

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

public class PedidoAdapter extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder> {

    private List<Pedido> pedidos;
    private Context context;
    private List<Tienda> listaTiendas;

    public PedidoAdapter(List<Pedido> pedidos, Context context, List<Tienda> listaTiendas) {
        this.pedidos = pedidos;
        this.context = context;
        this.listaTiendas = listaTiendas;
    }

    @Override
    public PedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);

        // Buscar el nombre de la tienda correspondiente usando el idTiendaFK
        String nombreTienda = obtenerNombreTienda(pedido.getIdTiendaFK());

        // Mostrar la fecha ya formateada
        holder.tienda.setText("Tienda: " + nombreTienda);
        holder.fecha.setText("Fecha Estimada: " + pedido.getFechaEstimada());  // Mostramos la fecha formateada
        holder.descripcion.setText("Descripción: " + pedido.getDescripcionPedido());

        // Clic para editar el pedido
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarPedidoActivity.class);
            intent.putExtra("idPedido", pedido.getIdPedido());
            intent.putExtra("descripcionPedido", pedido.getDescripcionPedido());
            intent.putExtra("importePedido", pedido.getImportePedido());
            intent.putExtra("tienda", pedido.getIdTiendaFK());
            intent.putExtra("fechaPedido", pedido.getFechaEstimada());  // Enviar la fecha estimada a la actividad de edición
            context.startActivity(intent);
        });

        // Mantener presionado para eliminar
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Pedido")
                    .setMessage("¿Seguro que deseas eliminar este pedido?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminarPedido(pedido.getIdPedido(), v.getContext(), position))
                    .setNegativeButton("Cancelar", null)
                    .show();
            return true;
        });
    }


    // Método para formatear la fecha de formato americano a formato europeo (MM/dd/yyyy a dd/MM/yyyy)
    private String formatearFecha(String fechaOriginal) {
        try {
            // Formato original (MM/dd/yyyy) si la fecha llega en ese formato
            SimpleDateFormat originalFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = originalFormat.parse(fechaOriginal);

            // Convertir a formato europeo (dd/MM/yyyy)
            SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
            return newFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return fechaOriginal; // Si hay error, retornamos la fecha original
        }
    }

    private String obtenerNombreTienda(int idTienda) {
        for (Tienda tienda : listaTiendas) {
            if (tienda.getIdTienda() == idTienda) {
                return tienda.getNombreTienda();
            }
        }
        return "Tienda no encontrada";
    }

    private void eliminarPedido(int idPedido, Context context, int position) {
        OkHttpClient client = new OkHttpClient();
        String url = "http://192.168.0.11/ApiRest/pedidos.php?idPedido=" + idPedido;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Error al eliminar el pedido", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        pedidos.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Pedido eliminado con éxito", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public static class PedidoViewHolder extends RecyclerView.ViewHolder {

        TextView tienda, fecha, descripcion;

        public PedidoViewHolder(View itemView) {
            super(itemView);
            tienda = itemView.findViewById(R.id.txtTienda);
            fecha = itemView.findViewById(R.id.txtFecha);
            descripcion = itemView.findViewById(R.id.txtDescripcion);
        }
    }
}