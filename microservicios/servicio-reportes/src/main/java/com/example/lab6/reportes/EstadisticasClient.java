package com.example.lab6.reportes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Este nombre debe coincidir con spring.application.name del proveedor.
@FeignClient(name = "servicio-estadisticas")
public interface EstadisticasClient {
    @PostMapping("/estadisticas")
    ResumenRespuesta calcular(@RequestBody SolicitudReporte solicitud);
}
