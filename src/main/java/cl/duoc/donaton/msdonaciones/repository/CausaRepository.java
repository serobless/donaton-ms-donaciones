package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Causa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CausaRepository extends JpaRepository<Causa, Long> {

    List<Causa> findByActivaTrue();
}
