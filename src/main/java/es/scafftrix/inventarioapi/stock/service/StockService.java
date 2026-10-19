package es.scafftrix.inventarioapi.stock.service;

import es.scafftrix.inventarioapi.stock.api.StockConsulta;
import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import es.scafftrix.inventarioapi.stock.entity.StockEntity;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaException;
import es.scafftrix.inventarioapi.stock.mapper.ReferenciaMapper;
import es.scafftrix.inventarioapi.stock.mapper.StockMapper;
import es.scafftrix.inventarioapi.stock.repository.ReferenciaRepository;
import es.scafftrix.inventarioapi.stock.repository.StockRepository;
import org.openapitools.model.NivelStock;
import org.openapitools.model.Referencia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Transactional
@Service
public class StockService implements StockConsulta {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);

    private final ReferenciaRepository referencias;
    private final StockRepository stock;
    private final ReferenciaMapper referenciaMapper;
    private final StockMapper stockMapper;

    public StockService(ReferenciaRepository referencias, StockRepository stock,
                        ReferenciaMapper referenciaMapper, StockMapper stockMapper) {
        this.referencias = referencias;
        this.stock = stock;
        this.referenciaMapper = referenciaMapper;
        this.stockMapper = stockMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Referencia> referencias() {
        return referencias.findAll().stream().map(referenciaMapper::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Referencia referencia(String sku) {
        return referencias.findById(sku)
                .map(referenciaMapper::toModel)
                .orElseThrow(() -> new ReferenciaException("Referencia " + sku + " no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelStock> stockPorSku(String sku) {
        referencia(sku);
        return stock.findBySkuOrderByAlmacenAsc(sku).stream().map(stockMapper::toModel).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int stockTotal(String sku) {
        return stock.findBySkuOrderByAlmacenAsc(sku).stream().mapToInt(StockEntity::getCantidad).sum();
    }

    @Override
    public void aplicarMovimiento(String sku, String almacen, int delta) {
        StockEntity fila = stock.findBySkuAndAlmacen(sku, almacen).orElse(null);
        if (fila == null) {
            if (delta < 0) {
                throw new IllegalStateException("No hay stock de " + sku + " registrado en el almacén " + almacen);
            }
            fila = new StockEntity();
            fila.setSku(sku);
            fila.setAlmacen(almacen);
            fila.setCantidad(0);
        }
        int cantidadAnterior = fila.getCantidad();
        int nuevaCantidad = cantidadAnterior + delta;
        if (nuevaCantidad < 0) {
            throw new IllegalStateException("El stock de " + sku + " en " + almacen + " no puede quedar negativo");
        }
        fila.setCantidad(nuevaCantidad);
        stock.save(fila);
        logger.debug("Stock aplicado: sku={}, almacén={}, {} -> {}", sku, almacen, cantidadAnterior, nuevaCantidad);
    }
}
