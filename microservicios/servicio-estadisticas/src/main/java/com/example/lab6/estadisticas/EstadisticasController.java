package com.example.lab6.estadisticas;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EstadisticasController {
    private final EstadisticasService service;
    public EstadisticasController(EstadisticasService service) { this.service = service; }
    @PostMapping("/estadisticas")
    public ResumenRespuesta calcular(@RequestBody SolicitudReporte solicitud) {
        return service.calcular(solicitud);
    }
}
