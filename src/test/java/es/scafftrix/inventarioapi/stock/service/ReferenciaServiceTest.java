package es.scafftrix.inventarioapi.stock.service;

import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaDuplicadaException;
import es.scafftrix.inventarioapi.stock.exception.ReferenciaException;
import es.scafftrix.inventarioapi.stock.mapper.ReferenciaMapper;
import es.scafftrix.inventarioapi.stock.repository.ReferenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.model.Referencia;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenciaServiceTest {

    @Mock
    private ReferenciaRepository repository;

    @Mock
    private ReferenciaMapper mapper;

    @InjectMocks
    private ReferenciaService service;

    private ReferenciaEntity entidad(String sku, String nombre, int stockMinimo) {
        ReferenciaEntity entidad = new ReferenciaEntity();
        entidad.setSku(sku);
        entidad.setNombre(nombre);
        entidad.setStockMinimo(stockMinimo);
        return entidad;
    }

    @Test
    void findAllReturnsEveryReferenceAsModel() {
        ReferenciaEntity entidad = entidad("SKU-1", "Tornillo", 10);
        Referencia modelo = new Referencia().sku("SKU-1").nombre("Tornillo").stockMinimo(10);

        when(repository.findAll()).thenReturn(List.of(entidad));
        when(mapper.toModel(entidad)).thenReturn(modelo);

        assertThat(service.findAll()).containsExactly(modelo);
    }

    @Test
    void findByIdThrowsWhenTheReferenceDoesNotExist() {
        when(repository.findById("NO-EXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("NO-EXISTE"))
                .isInstanceOf(ReferenciaException.class)
                .hasMessageContaining("NO-EXISTE");
    }

    @Test
    void createSavesTheMappedEntityAndReturnsTheModel() {
        Referencia modelo = new Referencia().sku("SKU-2").nombre("Caja").stockMinimo(5);
        ReferenciaEntity entidad = entidad("SKU-2", "Caja", 5);

        when(repository.existsById("SKU-2")).thenReturn(false);
        when(mapper.toEntity(modelo)).thenReturn(entidad);
        when(repository.save(entidad)).thenReturn(entidad);
        when(mapper.toModel(entidad)).thenReturn(modelo);

        assertThat(service.create(modelo)).isEqualTo(modelo);
        verify(repository).save(entidad);
    }

    @Test
    void createThrowsWhenTheSkuAlreadyExists() {
        Referencia modelo = new Referencia().sku("SKU-1").nombre("Duplicada");

        when(repository.existsById("SKU-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(modelo))
                .isInstanceOf(ReferenciaDuplicadaException.class);
    }

    @Test
    void updateThrowsWhenTheReferenceDoesNotExist() {
        when(repository.findById("NO-EXISTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("NO-EXISTE", new Referencia().sku("X").nombre("Y")))
                .isInstanceOf(ReferenciaException.class);
    }

    @Test
    void updateKeepsTheSkuFromThePath() {
        Referencia cuerpo = new Referencia().sku("OTRO-SKU").nombre("Actualizada").stockMinimo(3);
        ReferenciaEntity existente = entidad("SKU-ORIGINAL", "Vieja", 1);
        ReferenciaEntity mapeada = entidad("OTRO-SKU", "Actualizada", 3);

        when(repository.findById("SKU-ORIGINAL")).thenReturn(Optional.of(existente));
        when(mapper.toEntity(cuerpo)).thenReturn(mapeada);
        when(repository.save(any(ReferenciaEntity.class))).thenReturn(mapeada);
        when(mapper.toModel(mapeada)).thenReturn(cuerpo);

        service.update("SKU-ORIGINAL", cuerpo);

        assertThat(mapeada.getSku()).isEqualTo("SKU-ORIGINAL");
    }
}
