package es.scafftrix.inventarioapi.movimientos.service;

import es.scafftrix.inventarioapi.movimientos.entity.MovimientoEntity;
import es.scafftrix.inventarioapi.movimientos.exception.MovimientoException;
import es.scafftrix.inventarioapi.movimientos.exception.StockInsuficienteException;
import es.scafftrix.inventarioapi.movimientos.mapper.MovimientoMapper;
import es.scafftrix.inventarioapi.movimientos.repository.MovimientoRepository;
import es.scafftrix.inventarioapi.stock.api.StockConsulta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.Movimiento;
import org.openapitools.model.NivelStock;
import org.openapitools.model.PaginaMovimientos;
import org.openapitools.model.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository repository;

    @Mock
    private MovimientoMapper mapper;

    @Mock
    private StockConsulta stock;

    @InjectMocks
    private MovimientoService service;

    private Movimiento movimiento(TipoMovimiento tipo, String sku, String almacen, int cantidad) {
        return new Movimiento().tipo(tipo).sku(sku).almacen(almacen).cantidad(cantidad);
    }

    private NivelStock nivel(String almacen, int cantidad) {
        return new NivelStock().sku("SKU-1").almacen(almacen).cantidad(cantidad);
    }

    @Test
    void salidaWithoutEnoughStockRaisesConflict() {
        Movimiento movimiento = movimiento(TipoMovimiento.SALIDA, "SKU-1", "CENTRAL", 50);
        when(stock.stockPorSku("SKU-1")).thenReturn(List.of(nivel("CENTRAL", 10)));

        assertThatThrownBy(() -> service.create(movimiento))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("disponibles 10");

        verify(stock, never()).aplicarMovimiento(anyString(), anyString(), anyInt());
        verify(repository, never()).save(any(MovimientoEntity.class));
    }

    @Test
    void salidaWithEnoughStockAppliesANegativeDelta() {
        Movimiento movimiento = movimiento(TipoMovimiento.SALIDA, "SKU-1", "CENTRAL", 30);
        MovimientoEntity entidad = new MovimientoEntity();
        when(stock.stockPorSku("SKU-1")).thenReturn(List.of(nivel("CENTRAL", 100)));
        when(mapper.toEntity(movimiento)).thenReturn(entidad);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toModel(entidad)).thenReturn(movimiento);

        service.create(movimiento);

        verify(stock).aplicarMovimiento("SKU-1", "CENTRAL", -30);
    }

    @Test
    void entradaAppliesAPositiveDeltaAndStampsTheTimestamp() {
        Movimiento movimiento = movimiento(TipoMovimiento.ENTRADA, "SKU-1", "NORTE", 5);
        MovimientoEntity entidad = new MovimientoEntity();
        when(stock.stockPorSku("SKU-1")).thenReturn(List.of(nivel("CENTRAL", 100)));
        when(mapper.toEntity(movimiento)).thenReturn(entidad);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toModel(entidad)).thenReturn(movimiento);

        service.create(movimiento);

        verify(stock).aplicarMovimiento("SKU-1", "NORTE", 5);
        assertThat(entidad.getFecha()).isNotNull();
    }

    @Test
    void findByIdThrowsWhenTheMovementDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(MovimientoException.class);
    }

    @Test
    void findAllReturnsTheNewestMovementsFirst() {
        MovimientoEntity entidad = new MovimientoEntity();
        entidad.setId(7L);
        Page<MovimientoEntity> paginaRepositorio = new PageImpl<>(List.of(entidad), PageRequest.of(0, 20), 1);
        when(repository.findAll(any(Pageable.class))).thenReturn(paginaRepositorio);
        when(mapper.toModel(entidad)).thenReturn(new Movimiento().id(7L));

        PaginaMovimientos pagina = service.findAll(0, 20);

        assertThat(pagina.getPage()).isZero();
        assertThat(pagina.getSize()).isEqualTo(20);
        assertThat(pagina.getTotalElements()).isEqualTo(1);
        assertThat(pagina.getItems()).hasSize(1);

        ArgumentCaptor<Pageable> paginacion = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(paginacion.capture());
        assertThat(paginacion.getValue().getSort()).containsExactly(Sort.Order.desc("id"));
    }

    @Test
    void findAllClampsInvalidPageSizes() {
        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 0));

        PaginaMovimientos pagina = service.findAll(-5, 0);

        assertThat(pagina.getPage()).isZero();
        assertThat(pagina.getSize()).isOne();
    }
}
