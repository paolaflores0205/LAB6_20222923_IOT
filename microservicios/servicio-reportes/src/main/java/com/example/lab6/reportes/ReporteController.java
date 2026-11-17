package com.example.lab6.reportes;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;

@RestController
public class ReporteController {
    private final EstadisticasClient client;
    public ReporteController(EstadisticasClient client) { this.client = client; }
    @PostMapping("/reportes")
    public ResumenRespuesta crear(@RequestBody SolicitudReporte solicitud) {
        try {
            ResumenRespuesta resumen = client.calcular(solicitud);
            // Reportes prepara el mensaje; todas las sumas las hace Estadísticas.
            String curso = solicitud.getCurso();
            String alcance = curso == null || curso.isBlank() ? "Todos los cursos" : curso;
            resumen.setMensaje(String.format(new Locale("es", "PE"),
                    "%s: %d sesiones · %.2f horas", alcance, resumen.getCantidad(), resumen.getTotalHoras()));
            return resumen;
        } catch (FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revisa los datos de las sesiones.");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Estadísticas no está disponible. Revisa el servicio y su registro en Eureka.");
        }
    }
}
