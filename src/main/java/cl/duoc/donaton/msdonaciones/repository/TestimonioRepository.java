package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Testimonio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestimonioRepository extends JpaRepository<Testimonio, Long> {
    boolean existsByAutorId(String autorId);
}
