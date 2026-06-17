package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface CentroAcopioRepository extends JpaRepository<CentroAcopio, Long> {
    List<CentroAcopio> findByActivoTrue();

    @Modifying
    @Transactional
    @Query("UPDATE CentroAcopio c SET c.latitud = :lat, c.longitud = :lng " +
           "WHERE c.nombre = :nombre AND (c.latitud IS NULL OR c.latitud > 90 OR c.latitud < -90)")
    int corregirCoordenadas(@Param("nombre") String nombre,
                            @Param("lat") Double lat,
                            @Param("lng") Double lng);

    @Modifying
    @Transactional
    @Query("UPDATE CentroAcopio c SET c.capacidadActual = " +
           "CASE WHEN (c.capacidadActual + :cantidad) > c.capacidadMax THEN c.capacidadMax " +
           "     ELSE (c.capacidadActual + :cantidad) END " +
           "WHERE c.id = :id AND c.capacidadActual IS NOT NULL AND c.capacidadMax IS NOT NULL")
    int incrementarCapacidad(@Param("id") Long id, @Param("cantidad") int cantidad);
}
