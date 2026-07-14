package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.ItemDonacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemDonacionRepository extends JpaRepository<ItemDonacion, Long> {
    void deleteByDonacionIn(List<Donacion> donaciones);
}
