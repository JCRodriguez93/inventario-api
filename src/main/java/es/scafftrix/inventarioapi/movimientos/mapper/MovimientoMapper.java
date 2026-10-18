package es.scafftrix.inventarioapi.movimientos.mapper;

import es.scafftrix.inventarioapi.movimientos.entity.MovimientoEntity;
import org.mapstruct.Mapper;
import org.openapitools.model.Movimiento;
import org.openapitools.model.TipoMovimiento;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {

    Movimiento toModel(MovimientoEntity entity);

    MovimientoEntity toEntity(Movimiento model);

    default String toTipoNombre(TipoMovimiento tipo) {
        return tipo == null ? null : tipo.name();
    }

    default TipoMovimiento toTipo(String nombre) {
        return nombre == null ? null : TipoMovimiento.valueOf(nombre);
    }

    default OffsetDateTime toOffsetDateTime(LocalDateTime fecha) {
        return fecha == null ? null : fecha.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    default LocalDateTime toLocalDateTime(OffsetDateTime fecha) {
        return fecha == null ? null : fecha.toLocalDateTime();
    }
}
