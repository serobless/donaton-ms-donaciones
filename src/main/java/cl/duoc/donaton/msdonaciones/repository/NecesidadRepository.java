package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Necesidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NecesidadRepository extends JpaRepository<Necesidad, Long> {
    List<Necesidad> findByCentroIdAndActivaTrue(Long centroId);
    List<Necesidad> findByActivaTrue();
}
