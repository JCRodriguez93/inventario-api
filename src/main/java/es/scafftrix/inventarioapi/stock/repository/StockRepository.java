package es.scafftrix.inventarioapi.stock.repository;

import es.scafftrix.inventarioapi.stock.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockEntity, Long> {

    List<StockEntity> findBySkuOrderByAlmacenAsc(String sku);

    Optional<StockEntity> findBySkuAndAlmacen(String sku, String almacen);
}
