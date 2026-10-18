package es.scafftrix.inventarioapi.stock.controller;

import es.scafftrix.inventarioapi.stock.service.ReferenciaService;
import jakarta.validation.Valid;
import org.openapitools.api.ReferenciasApi;
import org.openapitools.model.Referencia;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReferenciasController implements ReferenciasApi {

    private final ReferenciaService service;

    public ReferenciasController(ReferenciaService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<Referencia>> getReferencias() {
        return ResponseEntity.ok(service.findAll());
    }

    @Override
    public ResponseEntity<Referencia> createReferencia(@Valid Referencia referencia) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(referencia));
    }

    @Override
    public ResponseEntity<Referencia> updateReferencia(String sku, @Valid Referencia referencia) {
        return ResponseEntity.ok(service.update(sku, referencia));
    }
}
