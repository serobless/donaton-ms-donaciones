package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.ItemDonacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDonacionRepository extends JpaRepository<ItemDonacion, Long> {
}
