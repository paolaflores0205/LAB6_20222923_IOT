package com.example.lab6.estadisticas;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EstadisticasServiceTest {
    private final EstadisticasService service = new EstadisticasService();
    private SesionEntrada sesion(String curso, String fecha, double horas, String tipo) {
        SesionEntrada s = new SesionEntrada();
        s.setCurso(curso); s.setFecha(fecha); s.setHorasDedicadas(horas); s.setTipoActividad(tipo);
        return s;
    }
    private SolicitudReporte solicitud(String curso, SesionEntrada... sesiones) {
        SolicitudReporte s = new SolicitudReporte(); s.setCurso(curso); s.setSesiones(List.of(sesiones)); return s;
    }
    @Test void filtraCursoYConservaDecimales() {
        ResumenRespuesta r = service.calcular(solicitud("IoT",
            sesion("IoT", "2026-11-17", 1.5, "Teoría"),
            sesion("IoT", "2026-11-18", 2, "Laboratorio"),
            sesion("Redes", "2026-10-12", 3, "Práctica")));
        assertEquals(2, r.getCantidad()); assertEquals(3.5, r.getTotalHoras(), 0.00001);
        assertEquals(3.5, r.getHorasPorMes().get("2026-11"), 0.00001);
        assertEquals(1.5, r.getHorasPorActividad().get("Teoría"), 0.00001);
        assertEquals(0, r.getHorasPorActividad().get("Práctica"), 0.00001);
    }
    @Test void separaMesesDeDistintosAnios() {
        ResumenRespuesta r = service.calcular(solicitud(null,
            sesion("IoT", "2026-11-17", 1.5, "Teoría"),
            sesion("IoT", "2025-11-18", 2, "Laboratorio")));
        assertEquals(2, r.getHorasPorMes().size());
        assertEquals(List.of("2025-11", "2026-11"), List.copyOf(r.getHorasPorMes().keySet()));
    }
    @Test void listaVaciaDevuelveTotalesCero() {
        ResumenRespuesta r = service.calcular(solicitud(null));
        assertEquals(0, r.getCantidad()); assertEquals(0, r.getTotalHoras());
        assertTrue(r.getHorasPorMes().isEmpty()); assertEquals(3, r.getHorasPorActividad().size());
    }
    @Test void todosLosCursosCoincidenConLasSumas() {
        ResumenRespuesta r = service.calcular(solicitud(null,
            sesion("IoT", "2026-11-17", 1.5, "Teoría"),
            sesion("Redes", "2026-10-12", 3, "Práctica")));
        assertEquals(4.5, r.getTotalHoras(), 0.00001);
        assertEquals(r.getTotalHoras(), r.getHorasPorMes().values().stream().mapToDouble(Double::doubleValue).sum(), 0.00001);
        assertEquals(r.getTotalHoras(), r.getHorasPorActividad().values().stream().mapToDouble(Double::doubleValue).sum(), 0.00001);
    }
    @Test void rechazaHorasInvalidas() {
        for (double horas : new double[]{0, -1, 25, Double.NaN, Double.POSITIVE_INFINITY}) {
            ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.calcular(solicitud(null, sesion("IoT", "2026-11-17", horas, "Teoría"))));
            assertEquals(400, error.getStatusCode().value());
        }
    }
    @Test void rechazaFechaActividadYCursoInvalidos() {
        assertThrows(ResponseStatusException.class, () -> service.calcular(solicitud(null, sesion("IoT", "2026-02-30", 1, "Teoría"))));
        assertThrows(ResponseStatusException.class, () -> service.calcular(solicitud(null, sesion("IoT", "2026-11-17", 1, "Otro"))));
        assertThrows(ResponseStatusException.class, () -> service.calcular(solicitud(null, sesion(" ", "2026-11-17", 1, "Teoría"))));
    }
    @Test void rechazaListaAusenteYFiltroSinCoincidenciasDevuelveCero() {
        assertThrows(ResponseStatusException.class, () -> service.calcular(new SolicitudReporte()));
        ResumenRespuesta r = service.calcular(solicitud("Otro", sesion("IoT", "2026-11-17", 1, "Teoría")));
        assertEquals(0, r.getCantidad());
    }
}
