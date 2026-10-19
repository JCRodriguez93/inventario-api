package es.scafftrix.inventarioapi.stock.service;

import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaDuplicadaException;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaException;
import es.scafftrix.inventarioapi.stock.mapper.ReferenciaMapper;
import es.scafftrix.inventarioapi.stock.repository.ReferenciaRepository;
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
public class ReferenciaService {

    private static final Logger logger = LoggerFactory.getLogger(ReferenciaService.class);

    private final ReferenciaRepository repository;
    private final ReferenciaMapper mapper;

    public ReferenciaService(ReferenciaRepository repository, ReferenciaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Referencia> findAll() {
        logger.debug("Consultando todas las referencias");
        return repository.findAll().stream().map(mapper::toModel).toList();
    }

    @Transactional(readOnly = true)
    public Referencia findById(String sku) {
        logger.debug("Buscando referencia con sku={}", sku);
        return repository.findById(sku)
                .map(mapper::toModel)
                .orElseThrow(() -> new ReferenciaException("Referencia " + sku + " no encontrada"));
    }

    public Referencia create(Referencia referencia) {
        if (repository.existsById(referencia.getSku())) {
            throw new ReferenciaDuplicadaException("Ya existe una referencia con SKU " + referencia.getSku());
        }
        Referencia guardada = mapper.toModel(repository.save(mapper.toEntity(referencia)));
        logger.info("Referencia creada: sku={}, stockMinimo={}", guardada.getSku(), guardada.getStockMinimo());
        return guardada;
    }

    public Referencia update(String sku, Referencia referencia) {
        ReferenciaEntity existing = repository.findById(sku)
                .orElseThrow(() -> new ReferenciaException("Referencia " + sku + " no encontrada"));
        ReferenciaEntity updated = mapper.toEntity(referencia);
        updated.setSku(existing.getSku());
        Referencia guardada = mapper.toModel(repository.save(updated));
        logger.info("Referencia actualizada: sku={}, stockMinimo={}", guardada.getSku(), guardada.getStockMinimo());
        return guardada;
    }
}
