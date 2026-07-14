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

    @Modifying
    @Transactional
    @Query(value = "UPDATE causas SET fecha_inicio = :inicio WHERE fecha_inicio IS NULL AND nombre = :titulo", nativeQuery = true)
    int asignarFechaInicio(@Param("titulo") String titulo, @Param("inicio") java.time.LocalDate inicio);

    @Modifying
    @Transactional
    @Query(value = "UPDATE causas SET fecha_fin = :fin WHERE fecha_fin IS NULL AND nombre = :titulo", nativeQuery = true)
    int asignarFechaFin(@Param("titulo") String titulo, @Param("fin") java.time.LocalDate fin);

    // Desmarca TODAS las causas destacadas (clearAutomatically evita que el cache de Hibernate
    // devuelva valores obsoletos tras el bulk update)
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Causa c SET c.destacada = false WHERE c.destacada = true")
    void desmarcarTodasDestacadas();

    // Cierra causas cuya fecha de fin ya pasó
    @Modifying
    @Transactional
    @Query("UPDATE Causa c SET c.activa = false WHERE c.activa = true AND c.fechaFin IS NOT NULL AND c.fechaFin < :hoy")
    int cerrarVencidas(@Param("hoy") java.time.LocalDate hoy);

    // Cierra causas que alcanzaron o superaron su meta de recaudación
    @Modifying
    @Transactional
    @Query("UPDATE Causa c SET c.activa = false WHERE c.activa = true AND c.recaudado >= c.meta")
    int cerrarCompletadas();
}
