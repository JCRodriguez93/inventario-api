package es.scafftrix.inventarioapi.movimientos.controller;

import es.scafftrix.inventarioapi.movimientos.service.MovimientoService;
import jakarta.validation.Valid;
import org.openapitools.api.MovimientosApi;
import org.openapitools.model.Movimiento;
import org.openapitools.model.PaginaMovimientos;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MovimientosController implements MovimientosApi {

    private final MovimientoService service;

    public MovimientosController(MovimientoService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<Movimiento> createMovimiento(@Valid Movimiento movimiento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(movimiento));
    }

    @Override
    public ResponseEntity<Movimiento> getMovimientoPorId(Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Override
    public ResponseEntity<PaginaMovimientos> getMovimientos(Integer page, Integer size) {
        return ResponseEntity.ok(service.findAll(page, size));
    }
}
