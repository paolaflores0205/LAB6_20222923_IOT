package com.example.lab6;
import java.util.TreeMap;

// Datos recibidos del backend. Android los usa para dibujar, sin volver a sumarlos.
public class ResumenEstudio {
    public final TreeMap<String, Double> porMes = new TreeMap<>();
    public final double[] porTipo = new double[3];
    public double total;
    public int cantidad;
    public static final String[] TIPOS = {"Teoría", "Práctica", "Laboratorio"};
}
