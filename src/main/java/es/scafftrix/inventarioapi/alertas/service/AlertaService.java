package es.scafftrix.inventarioapi.alertas.service;

import es.scafftrix.inventarioapi.stock.api.StockConsulta;
import org.openapitools.model.Alerta;
import org.openapitools.model.Referencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Transactional(readOnly = true)
@Service
public class AlertaService {

    private final StockConsulta stock;

    public AlertaService(StockConsulta stock) {
        this.stock = stock;
    }

    public List<Alerta> findAll() {
        List<Alerta> alertas = new ArrayList<>();
        for (Referencia referencia : stock.referencias()) {
            int stockTotal = stock.stockTotal(referencia.getSku());
            if (stockTotal < referencia.getStockMinimo()) {
                Alerta alerta = new Alerta();
                alerta.setSku(referencia.getSku());
                alerta.setNombre(referencia.getNombre());
                alerta.setStockTotal(stockTotal);
                alerta.setStockMinimo(referencia.getStockMinimo());
                alertas.add(alerta);
            }
        }
        return alertas;
    }
}
