package cl.duoc.donaton.msdonaciones.repository;

import cl.duoc.donaton.msdonaciones.model.Donacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    // Top 10 donantes por monto total acumulado
    @Query("""
        SELECT d.donanteAlias, SUM(d.monto) AS totalMonto
        FROM Donacion d
        WHERE d.donanteAlias IS NOT NULL
        GROUP BY d.donanteAlias
        ORDER BY totalMonto DESC
        LIMIT 10
        """)
    List<Object[]> findTop10Donantes();

    List<Donacion> findByDonadorId(String donadorId);

    List<Donacion> findAllByOrderByFechaDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.monto), 0) FROM Donacion d")
    BigDecimal sumTotalMontos();
}
