package es.scafftrix.inventarioapi.alertas.service;

import es.scafftrix.inventarioapi.stock.api.StockConsulta;
import org.openapitools.model.Alerta;
import org.openapitools.model.Referencia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@Transactional(readOnly = true)
@Service
public class AlertaService {

    private static final Logger logger = LoggerFactory.getLogger(AlertaService.class);

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
        if (alertas.isEmpty()) {
            logger.debug("Consulta de alertas: sin referencias por debajo del mínimo");
        } else {
            logger.info("Consulta de alertas: {} referencias por debajo del mínimo: {}",
                    alertas.size(), alertas.stream().map(Alerta::getSku).toList());
        }
        return alertas;
    }
}
