package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Donacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    // Top N donantes por monto total acumulado
    @Query("""
        SELECT d.donanteAlias, SUM(d.monto) AS totalMonto, COUNT(d.id) AS cantidad
        FROM Donacion d
        WHERE d.donanteAlias IS NOT NULL
        GROUP BY d.donanteAlias
        ORDER BY totalMonto DESC
        LIMIT :limit
        """)
    List<Object[]> findTopDonantes(@org.springframework.data.repository.query.Param("limit") int limit);

    List<Donacion> findByCausaId(Long causaId);

    List<Donacion> findByDonadorId(String donadorId);

    List<Donacion> findByCentroAcopioIdOrderByFechaDesc(Long centroId);

    List<Donacion> findAllByOrderByFechaDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.monto), 0) FROM Donacion d")
    BigDecimal sumTotalMontos();
}
