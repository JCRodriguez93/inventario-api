package es.scafftrix.inventarioapi.stock.controller;

import es.scafftrix.inventarioapi.stock.service.StockService;
import org.openapitools.api.StockApi;
import org.openapitools.model.NivelStock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StockController implements StockApi {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<NivelStock>> getStockPorSku(String sku) {
        return ResponseEntity.ok(service.stockPorSku(sku));
    }
}
