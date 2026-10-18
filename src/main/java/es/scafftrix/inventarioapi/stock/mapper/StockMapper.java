package es.scafftrix.inventarioapi.stock.mapper;

import es.scafftrix.inventarioapi.stock.entity.StockEntity;
import org.mapstruct.Mapper;
import org.openapitools.model.NivelStock;

@Mapper(componentModel = "spring")
public interface StockMapper {

    NivelStock toModel(StockEntity entity);

    StockEntity toEntity(NivelStock model);
}
