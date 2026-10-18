package es.scafftrix.inventarioapi.movimientos.repository;

import es.scafftrix.inventarioapi.movimientos.entity.MovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long> {
}
