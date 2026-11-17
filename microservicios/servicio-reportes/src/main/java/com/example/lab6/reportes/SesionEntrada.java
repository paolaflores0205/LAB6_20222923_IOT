package com.example.lab6.reportes;

// JSON sencillo: la fecha viaja como yyyy-MM-dd, no como objeto de Firebase.
public class SesionEntrada {
    private String curso;
    private String fecha;
    private Double horasDedicadas;
    private String tipoActividad;
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Double getHorasDedicadas() { return horasDedicadas; }
    public void setHorasDedicadas(Double horas) { this.horasDedicadas = horas; }
    public String getTipoActividad() { return tipoActividad; }
    public void setTipoActividad(String tipo) { this.tipoActividad = tipo; }
}
