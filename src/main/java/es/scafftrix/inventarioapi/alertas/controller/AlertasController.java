package es.scafftrix.inventarioapi.alertas.controller;

import es.scafftrix.inventarioapi.alertas.service.AlertaService;
import org.openapitools.api.AlertasApi;
import org.openapitools.model.Alerta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlertasController implements AlertasApi {

    private final AlertaService service;

    public AlertasController(AlertaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Alerta>> getAlertas() {
        return ResponseEntity.ok(service.findAll());
    }
}
