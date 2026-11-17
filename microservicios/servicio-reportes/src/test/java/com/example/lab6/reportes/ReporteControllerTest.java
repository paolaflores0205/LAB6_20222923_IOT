package com.example.lab6.reportes;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteControllerTest {
    @Test void usaElResultadoDelClienteYAgregaElMensaje() {
        ResumenRespuesta respuesta = new ResumenRespuesta();
        respuesta.setCantidad(2); respuesta.setTotalHoras(3.5);
        SolicitudReporte solicitud = new SolicitudReporte(); solicitud.setCurso("IoT");
        EstadisticasClient client = mock(EstadisticasClient.class);
        when(client.calcular(solicitud)).thenReturn(respuesta);
        ResumenRespuesta resultado = new ReporteController(client).crear(solicitud);
        verify(client).calcular(solicitud);
        assertSame(respuesta, resultado); assertEquals(3.5, resultado.getTotalHoras());
        assertTrue(resultado.getMensaje().contains("IoT"));
    }
    @Test void informa503SiElProveedorFalla() {
        EstadisticasClient client = mock(EstadisticasClient.class);
        SolicitudReporte solicitud = new SolicitudReporte();
        when(client.calcular(solicitud)).thenThrow(mock(FeignException.class));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> new ReporteController(client).crear(solicitud));
        assertEquals(503, error.getStatusCode().value());
    }
    @Test void conserva400ParaDatosInvalidos() {
        EstadisticasClient client = mock(EstadisticasClient.class);
        SolicitudReporte solicitud = new SolicitudReporte();
        when(client.calcular(solicitud)).thenThrow(mock(FeignException.BadRequest.class));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> new ReporteController(client).crear(solicitud));
        assertEquals(400, error.getStatusCode().value());
    }
}
