package es.studium.mispedidospendientes;

import com.google.gson.annotations.SerializedName;

public class Pedido {

    private int idPedido;
    private String descripcionPedido;
    private String fechaPedido;  // Fecha del pedido
    @SerializedName("fechaEstimadaPedido")
    private String fechaEstimada; // Fecha estimada del pedido
    private double importePedido;
    private int idTiendaFK; // Relación con Tienda
    private int estadoPedido; // Cambiado de int a boolean (true para entregado, false para no entregado)

    // Constructor
    public Pedido(int idPedido, String descripcionPedido, String fechaPedido, String fechaEstimada, double importePedido, int idTiendaFK, int estadoPedido) {
        this.idPedido = idPedido;
        this.descripcionPedido = descripcionPedido;
        this.fechaPedido = fechaPedido;
        this.fechaEstimada = fechaEstimada;
        this.importePedido = importePedido;
        this.idTiendaFK = idTiendaFK;
        this.estadoPedido = estadoPedido; // Inicializado como booleano
    }

    // Métodos getter y setter
    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public String getDescripcionPedido() {
        return descripcionPedido;
    }

    public void setDescripcionPedido(String descripcionPedido) {
        this.descripcionPedido = descripcionPedido;
    }

    public String getFechaPedido() {
        return fechaPedido;  // Fecha del pedido
    }

    public void setFechaPedido(String fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getFechaEstimada() {
        return fechaEstimada; // Método getter para la fecha estimada
    }

    public void setFechaEstimada(String fechaEstimada) {
        this.fechaEstimada = fechaEstimada; // Método setter para la fecha estimada
    }

    public double getImportePedido() {
        return importePedido;
    }

    public void setImportePedido(double importePedido) {
        this.importePedido = importePedido;
    }

    public int getIdTiendaFK() {
        return idTiendaFK;
    }

    public void setIdTiendaFK(int idTiendaFK) {
        this.idTiendaFK = idTiendaFK;
    }

    // Getter y Setter para estadoPedido
    public int isEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(int estadoPedido) {
        this.estadoPedido = estadoPedido;
    }
}
