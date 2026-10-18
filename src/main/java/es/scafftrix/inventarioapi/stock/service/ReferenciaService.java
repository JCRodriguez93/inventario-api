package es.scafftrix.inventarioapi.stock.service;

import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaDuplicadaException;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaException;
import es.scafftrix.inventarioapi.stock.mapper.ReferenciaMapper;
import es.scafftrix.inventarioapi.stock.repository.ReferenciaRepository;
import org.openapitools.model.Referencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Transactional
@Service
public class ReferenciaService {

    private final ReferenciaRepository repository;
    private final ReferenciaMapper mapper;

    public ReferenciaService(ReferenciaRepository repository, ReferenciaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Referencia> findAll() {
        return repository.findAll().stream().map(mapper::toModel).toList();
    }

    @Transactional(readOnly = true)
    public Referencia findById(String sku) {
        return repository.findById(sku)
                .map(mapper::toModel)
                .orElseThrow(() -> new ReferenciaException("Referencia " + sku + " no encontrada"));
    }

    public Referencia create(Referencia referencia) {
        if (repository.existsById(referencia.getSku())) {
            throw new ReferenciaDuplicadaException("Ya existe una referencia con SKU " + referencia.getSku());
        }
        return mapper.toModel(repository.save(mapper.toEntity(referencia)));
    }

    public Referencia update(String sku, Referencia referencia) {
        ReferenciaEntity existing = repository.findById(sku)
                .orElseThrow(() -> new ReferenciaException("Referencia " + sku + " no encontrada"));
        ReferenciaEntity updated = mapper.toEntity(referencia);
        updated.setSku(existing.getSku());
        return mapper.toModel(repository.save(updated));
    }
}
