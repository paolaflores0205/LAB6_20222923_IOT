package com.example.lab6;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import static org.junit.Assert.*;

// Prueba opcional: necesita los tres backends ejecutándose en la computadora del emulador.
public class ReporteApiIntegrationTest {
    @Before public void requiereBackend() {
        Assume.assumeTrue("Activa probarBackend=true solo cuando hayas iniciado los servicios.",
                "true".equals(InstrumentationRegistry.getArguments().getString("probarBackend")));
    }
    private Sesion sesion(String curso, double horas, String tipo) {
        Calendar fecha = Calendar.getInstance(); fecha.clear(); fecha.set(2026, Calendar.NOVEMBER, 17);
        Sesion s = new Sesion(); s.setCurso(curso); s.setFecha(fecha.getTime());
        s.setHorasDedicadas(horas); s.setTipoActividad(tipo); return s;
    }
    private List<Sesion> ejemplo() {
        return Arrays.asList(sesion("IoT", 1.5, "Teoría"), sesion("IoT", 2, "Laboratorio"), sesion("Redes", 3, "Práctica"));
    }
    @Test public void androidConsultaReportesYRecibeElFiltro() throws Exception {
        ReporteApi.Resultado r = ReporteApi.consultar("http://10.0.2.2:8020", ejemplo(), "IoT");
        assertEquals(2, r.resumen.cantidad); assertEquals(3.5, r.resumen.total, 0.00001);
        assertEquals(3.5, r.resumen.porMes.get("2026-11"), 0.00001);
        assertArrayEquals(new double[]{1.5, 0, 2}, r.resumen.porTipo, 0.00001);
        assertTrue(r.mensaje.contains("IoT"));
    }
    @Test public void androidPuedeSolicitarTodosLosCursos() throws Exception {
        ReporteApi.Resultado r = ReporteApi.consultar("http://10.0.2.2:8020", ejemplo(), null);
        assertEquals(3, r.resumen.cantidad); assertEquals(6.5, r.resumen.total, 0.00001);
    }
}
