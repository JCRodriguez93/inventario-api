package es.scafftrix.inventarioapi.movimientos.service;

import es.scafftrix.inventarioapi.movimientos.entity.MovimientoEntity;
import es.scafftrix.inventarioapi.movimientos.exception.MovimientoException;
import es.scafftrix.inventarioapi.movimientos.exception.StockInsuficienteException;
import es.scafftrix.inventarioapi.movimientos.mapper.MovimientoMapper;
import es.scafftrix.inventarioapi.movimientos.repository.MovimientoRepository;
import es.scafftrix.inventarioapi.stock.api.StockConsulta;
import org.openapitools.model.Movimiento;
import org.openapitools.model.NivelStock;
import org.openapitools.model.PaginaMovimientos;
import org.openapitools.model.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@Transactional
@Service
public class MovimientoService {

    private static final int TAMANO_MAXIMO_PAGINA = 100;

    private final MovimientoRepository repository;
    private final MovimientoMapper mapper;
    private final StockConsulta stock;

    public MovimientoService(MovimientoRepository repository, MovimientoMapper mapper, StockConsulta stock) {
        this.repository = repository;
        this.mapper = mapper;
        this.stock = stock;
    }

    public Movimiento create(Movimiento movimiento) {
        List<NivelStock> niveles = stock.stockPorSku(movimiento.getSku());
        if (movimiento.getTipo() == TipoMovimiento.SALIDA) {
            int disponible = niveles.stream()
                    .filter(nivel -> nivel.getAlmacen().equals(movimiento.getAlmacen()))
                    .mapToInt(NivelStock::getCantidad)
                    .sum();
            if (disponible < movimiento.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente en el almacén " + movimiento.getAlmacen()
                                + ": disponibles " + disponible + ", solicitado " + movimiento.getCantidad());
            }
        }
        int delta = movimiento.getTipo() == TipoMovimiento.ENTRADA
                ? movimiento.getCantidad()
                : -movimiento.getCantidad();
        stock.aplicarMovimiento(movimiento.getSku(), movimiento.getAlmacen(), delta);

        MovimientoEntity entity = mapper.toEntity(movimiento);
        entity.setFecha(LocalDateTime.now());
        return mapper.toModel(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public Movimiento findById(Long id) {
        return repository.findById(id)
                .map(mapper::toModel)
                .orElseThrow(() -> new MovimientoException("Movimiento " + id + " no encontrado"));
    }

    @Transactional(readOnly = true)
    public PaginaMovimientos findAll(Integer page, Integer size) {
        int numeroPagina = page == null ? 0 : Math.max(0, page);
        int tamano = size == null ? 20 : Math.min(Math.max(1, size), TAMANO_MAXIMO_PAGINA);

        Page<MovimientoEntity> resultado = repository.findAll(
                PageRequest.of(numeroPagina, tamano, Sort.by(Sort.Direction.DESC, "id")));

        PaginaMovimientos pagina = new PaginaMovimientos();
        pagina.setItems(resultado.getContent().stream().map(mapper::toModel).toList());
        pagina.setPage(resultado.getNumber());
        pagina.setSize(resultado.getSize());
        pagina.setTotalElements(resultado.getTotalElements());
        pagina.setTotalPages(resultado.getTotalPages());
        return pagina;
    }
}
