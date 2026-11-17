package com.example.lab6;

import org.junit.Test;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import static org.junit.Assert.*;

public class ResumenEstudioTest {
    private Sesion sesion(String curso, int year, int month, double horas, String tipo) {
        Calendar fecha = Calendar.getInstance();
        fecha.clear(); fecha.set(year, month - 1, 15);
        Sesion s = new Sesion();
        s.setCurso(curso); s.setFecha(fecha.getTime()); s.setHorasDedicadas(horas); s.setTipoActividad(tipo);
        return s;
    }

    @Test public void sumaDecimalesYFiltraLosDosGraficos() {
        ResumenEstudio r = ResumenEstudio.calcular(Arrays.asList(
                sesion("IoT", 2026, 10, 1.5, "Teoría"),
                sesion("IoT", 2026, 10, 2.0, "Laboratorio"),
                sesion("Redes", 2026, 10, 8.0, "Práctica")), "IoT");
        assertEquals(2, r.cantidad);
        assertEquals(3.5, r.total, 0.0001);
        assertEquals(3.5, r.porMes.get("2026-10"), 0.0001);
        assertArrayEquals(new double[]{1.5, 0, 2}, r.porTipo, 0.0001);
    }

    @Test public void separaElMismoMesDeDistintosAniosYOrdena() {
        ResumenEstudio r = ResumenEstudio.calcular(Arrays.asList(
                sesion("IoT", 2026, 1, 2, "Teoría"),
                sesion("IoT", 2025, 1, 3, "Teoría")), null);
        assertEquals(2, r.porMes.size());
        assertEquals("2025-01", r.porMes.firstKey());
        assertEquals(5, r.total, 0.0001);
    }

    @Test public void todosLosCursosCoincidenConLasSumasDeAmbosGraficos() {
        ResumenEstudio r = ResumenEstudio.calcular(Arrays.asList(
                sesion("IoT", 2026, 10, 1.5, "Teoría"),
                sesion("Redes", 2026, 11, 2.5, "Práctica")), null);
        double meses = 0, tipos = 0;
        for (double valor : r.porMes.values()) meses += valor;
        for (double valor : r.porTipo) tipos += valor;
        assertEquals(r.total, meses, 0.0001);
        assertEquals(r.total, tipos, 0.0001);
        assertEquals(4, r.total, 0.0001);
    }

    @Test public void filtroSinResultadosDevuelveCero() {
        ResumenEstudio r = ResumenEstudio.calcular(Collections.singletonList(
                sesion("IoT", 2026, 10, 2, "Teoría")), "Otro curso");
        assertEquals(0, r.cantidad);
        assertTrue(r.porMes.isEmpty());
        assertEquals(0, r.total, 0);
    }

    @Test public void omiteDatosInvalidosSinContaminarLosTotales() {
        ResumenEstudio r = ResumenEstudio.calcular(Arrays.asList(
                sesion("IoT", 2026, 10, Double.NaN, "Teoría"),
                sesion("IoT", 2026, 10, -2, "Teoría"),
                sesion("IoT", 2026, 10, 2, "Desconocido"), new Sesion()), null);
        assertEquals(0, r.total, 0);
        assertEquals(0, r.cantidad);
    }
}
