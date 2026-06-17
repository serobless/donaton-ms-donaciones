package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Causa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CausaRepository extends JpaRepository<Causa, Long> {

    List<Causa> findByActivaTrue();

    @Modifying
    @Transactional
    @Query("UPDATE Causa c SET c.titulo = REPLACE(c.titulo, :de, :a) WHERE c.titulo LIKE CONCAT('%', :de, '%')")
    int reemplazarEnTitulos(@Param("de") String de, @Param("a") String a);
}
