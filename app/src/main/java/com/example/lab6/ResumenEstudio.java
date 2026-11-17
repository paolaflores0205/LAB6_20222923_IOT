package com.example.lab6;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

// Cálculos independientes de Android: recorrer, filtrar y sumar.
public class ResumenEstudio {
    public final TreeMap<String, Double> porMes = new TreeMap<>();
    public final double[] porTipo = new double[3];
    public double total;
    public int cantidad;
    public static final String[] TIPOS = {"Teoría", "Práctica", "Laboratorio"};
    public static ResumenEstudio calcular(List<Sesion> sesiones, String curso) {
        ResumenEstudio resumen = new ResumenEstudio();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM", Locale.ROOT);
        for (Sesion sesion : sesiones) {
            if (curso != null && !curso.equals(sesion.getCurso())) continue;
            Double horas = sesion.getHorasDedicadas();
            if (sesion.getFecha() == null || horas == null || !Double.isFinite(horas) || horas <= 0) continue;
            int tipo = -1;
            for (int i = 0; i < TIPOS.length; i++) if (TIPOS[i].equals(sesion.getTipoActividad())) tipo = i;
            if (tipo == -1) continue;
            String mes = formato.format(sesion.getFecha());
            resumen.porMes.put(mes, resumen.porMes.getOrDefault(mes, 0.0) + horas);
            resumen.porTipo[tipo] += horas;
            resumen.total += horas;
            resumen.cantidad++;
        }
        return resumen;
    }
}
