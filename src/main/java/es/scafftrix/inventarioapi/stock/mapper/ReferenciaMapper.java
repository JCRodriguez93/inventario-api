package es.scafftrix.inventarioapi.stock.mapper;

import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import org.mapstruct.Mapper;
import org.openapitools.model.Referencia;

@Mapper(componentModel = "spring")
public interface ReferenciaMapper {

    Referencia toModel(ReferenciaEntity entity);

    ReferenciaEntity toEntity(Referencia model);
}
