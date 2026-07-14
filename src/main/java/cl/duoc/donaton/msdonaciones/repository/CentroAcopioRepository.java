package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface CentroAcopioRepository extends JpaRepository<CentroAcopio, Long> {
    List<CentroAcopio> findByActivoTrue();
    Optional<CentroAcopio> findByNombre(String nombre);

    // Verifica si ya existe otro centro con la misma dirección+ciudad (para crear)
    boolean existsByDireccionIgnoreCaseAndCiudadIgnoreCase(String direccion, String ciudad);

    // Verifica si ya existe OTRO centro (distinto id) con la misma dirección+ciudad (para editar)
    boolean existsByDireccionIgnoreCaseAndCiudadIgnoreCaseAndIdNot(String direccion, String ciudad, Long id);

    // Verifica si ya existe otro centro con las mismas coordenadas (para crear)
    @Query("SELECT COUNT(c) > 0 FROM CentroAcopio c WHERE ABS(c.latitud - :lat) < 0.0001 AND ABS(c.longitud - :lng) < 0.0001")
    boolean existsByCoordenadasCercanas(@Param("lat") Double lat, @Param("lng") Double lng);

    // Verifica si ya existe OTRO centro con las mismas coordenadas (para editar)
    @Query("SELECT COUNT(c) > 0 FROM CentroAcopio c WHERE ABS(c.latitud - :lat) < 0.0001 AND ABS(c.longitud - :lng) < 0.0001 AND c.id <> :id")
    boolean existsByCoordenadasCercanasAndIdNot(@Param("lat") Double lat, @Param("lng") Double lng, @Param("id") Long id);

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

    @Query(value = "SELECT COUNT(*) FROM centro_acopio_que_recibe WHERE centro_id = " +
                   "(SELECT id FROM centros_acopio WHERE nombre = :nombre)", nativeQuery = true)
    int countQueRecibeByNombre(@Param("nombre") String nombre);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM centro_acopio_que_recibe WHERE centro_id = " +
                   "(SELECT id FROM centros_acopio WHERE nombre = :nombre)", nativeQuery = true)
    int clearQueRecibeByNombre(@Param("nombre") String nombre);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO centro_acopio_que_recibe (centro_id, item) " +
                   "SELECT id, :item FROM centros_acopio WHERE nombre = :nombre", nativeQuery = true)
    int addQueRecibeByNombre(@Param("nombre") String nombre, @Param("item") String item);
}
