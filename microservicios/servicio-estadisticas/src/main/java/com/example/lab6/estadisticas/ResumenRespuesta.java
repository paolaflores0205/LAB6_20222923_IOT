package com.example.lab6.estadisticas;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class ResumenRespuesta {
    private int cantidad;
    private double totalHoras;
    private Map<String, Double> horasPorMes = new TreeMap<>();
    private Map<String, Double> horasPorActividad = new LinkedHashMap<>();
    private String mensaje;
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getTotalHoras() { return totalHoras; }
    public void setTotalHoras(double total) { this.totalHoras = total; }
    public Map<String, Double> getHorasPorMes() { return horasPorMes; }
    public void setHorasPorMes(Map<String, Double> valores) { this.horasPorMes = valores; }
    public Map<String, Double> getHorasPorActividad() { return horasPorActividad; }
    public void setHorasPorActividad(Map<String, Double> valores) { this.horasPorActividad = valores; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
