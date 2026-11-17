package com.example.lab6.estadisticas;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class EstadisticasService {
    public ResumenRespuesta calcular(SolicitudReporte solicitud) {
        if (solicitud == null || solicitud.getSesiones() == null) {
            throw error("Debes enviar una lista de sesiones.");
        }
        ResumenRespuesta resumen = new ResumenRespuesta();
        resumen.getHorasPorActividad().put("Teoría", 0.0);
        resumen.getHorasPorActividad().put("Práctica", 0.0);
        resumen.getHorasPorActividad().put("Laboratorio", 0.0);
        String filtro = solicitud.getCurso();
        if (filtro != null && filtro.isBlank()) filtro = null;
        for (SesionEntrada sesion : solicitud.getSesiones()) {
            // Se valida antes de usar los datos: nunca calculamos con horas inválidas.
            if (sesion == null || sesion.getCurso() == null || sesion.getCurso().isBlank()
                    || sesion.getCurso().length() > 100) throw error("Curso inválido.");
            Double horas = sesion.getHorasDedicadas();
            if (horas == null || !Double.isFinite(horas) || horas <= 0 || horas > 24) {
                throw error("Las horas deben ser mayores que 0 y como máximo 24.");
            }
            String tipo = sesion.getTipoActividad();
            if (!resumen.getHorasPorActividad().containsKey(tipo)) throw error("Actividad inválida.");
            LocalDate fecha;
            if (sesion.getFecha() == null) throw error("La fecha es obligatoria.");
            try { fecha = LocalDate.parse(sesion.getFecha()); }
            catch (DateTimeParseException e) {
                throw error("La fecha debe ser válida y tener formato yyyy-MM-dd.");
            }
            if (filtro != null && !filtro.equals(sesion.getCurso())) continue;
            String mes = fecha.toString().substring(0, 7);
            double acumuladoMes = resumen.getHorasPorMes().getOrDefault(mes, 0.0);
            resumen.getHorasPorMes().put(mes, acumuladoMes + horas);
            double acumuladoTipo = resumen.getHorasPorActividad().get(tipo);
            resumen.getHorasPorActividad().put(tipo, acumuladoTipo + horas);
            resumen.setCantidad(resumen.getCantidad() + 1);
            resumen.setTotalHoras(resumen.getTotalHoras() + horas);
        }
        return resumen;
    }
    private ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
