package com.example.lab6.reportes;
import java.util.List;

public class SolicitudReporte {
    private List<SesionEntrada> sesiones;
    private String curso; // null o vacío: todos los cursos.
    public List<SesionEntrada> getSesiones() { return sesiones; }
    public void setSesiones(List<SesionEntrada> sesiones) { this.sesiones = sesiones; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
}
