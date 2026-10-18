package es.scafftrix.inventarioapi.stock.repository;

import es.scafftrix.inventarioapi.stock.entity.ReferenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenciaRepository extends JpaRepository<ReferenciaEntity, String> {
}
