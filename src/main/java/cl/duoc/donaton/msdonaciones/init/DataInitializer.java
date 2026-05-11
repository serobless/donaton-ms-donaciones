package cl.duoc.donaton.msdonaciones.init;

import cl.duoc.donaton.msdonaciones.factory.DonacionFactory;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CausaRepository causaRepository;
    private final DonacionRepository donacionRepository;

    @Override
    public void run(String... args) {
        if (causaRepository.count() > 0) return;

        // --- 3 Causas ---
        Causa alimentacion = causaRepository.save(Causa.builder()
                .nombre("Alimentación para familias vulnerables")
                .descripcion("Proveemos cajas de alimentos mensuales a 150 familias en situación de calle en Santiago Centro.")
                .meta(new BigDecimal("5000000"))
                .recaudado(new BigDecimal("3250000"))
                .activa(true)
                .categoria("ALIMENTACION")
                .imagenUrl("https://images.unsplash.com/photo-1593113630400-ea4288922559?w=800")
                .diasRestantes(18)
                .build());

        Causa salud = causaRepository.save(Causa.builder()
                .nombre("Medicamentos para adultos mayores")
                .descripcion("Compra y distribución de medicamentos esenciales para adultos mayores sin previsión en la región Metropolitana.")
                .meta(new BigDecimal("2000000"))
                .recaudado(new BigDecimal("420000"))
                .activa(true)
                .categoria("SALUD")
                .imagenUrl("https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=800")
                .diasRestantes(45)
                .build());

        Causa ropa = causaRepository.save(Causa.builder()
                .nombre("Abrigo para invierno 2025")
                .descripcion("Recolección y distribución de ropa de abrigo para personas en situación de calle antes del invierno.")
                .meta(new BigDecimal("1500000"))
                .recaudado(new BigDecimal("1480000"))
                .activa(true)
                .categoria("VESTIMENTA")
                .imagenUrl("https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=800")
                .diasRestantes(7)
                .build());

        log.info("Causas creadas: {}, {}, {}", alimentacion.getNombre(), salud.getNombre(), ropa.getNombre());

        // --- 15 Donaciones ---
        List<Donacion> donaciones = List.of(
            buildDonacion(TipoDonacion.MONETARIA, "150000", "juanito88", alimentacion, -1),
            buildDonacion(TipoDonacion.MONETARIA, "75000",  "maria_sol", alimentacion, -3),
            buildDonacion(TipoDonacion.MONETARIA, "200000", "donador_anonimo", alimentacion, -5),
            buildDonacion(TipoDonacion.ALIMENTO,  "30000",  "club_rotario", alimentacion, -7),
            buildDonacion(TipoDonacion.ALIMENTO,  "45000",  "familia_perez", alimentacion, -10),

            buildDonacion(TipoDonacion.MONETARIA, "80000",  "carolina_v", salud, -2),
            buildDonacion(TipoDonacion.MONETARIA, "120000", "dr_rodrigo", salud, -4),
            buildDonacion(TipoDonacion.MEDICA,    "50000",  "farmacia_cruz", salud, -6),
            buildDonacion(TipoDonacion.MEDICA,    "35000",  "juanito88", salud, -8),
            buildDonacion(TipoDonacion.MONETARIA, "85000",  "maria_sol", salud, -12),

            buildDonacion(TipoDonacion.ROPA,      "0",      "centro_madre", ropa, -1),
            buildDonacion(TipoDonacion.ROPA,      "0",      "taller_costura", ropa, -3),
            buildDonacion(TipoDonacion.MONETARIA, "300000", "empresa_textil", ropa, -5),
            buildDonacion(TipoDonacion.ROPA,      "0",      "carolina_v", ropa, -9),
            buildDonacion(TipoDonacion.MONETARIA, "600000", "donador_grande", ropa, -15)
        );

        donacionRepository.saveAll(donaciones);
        log.info("DataInitializer: {} donaciones de prueba cargadas.", donaciones.size());
    }

    private Donacion buildDonacion(TipoDonacion tipo, String monto, String alias,
                                    Causa causa, int diasOffset) {
        Donacion d = DonacionFactory.crear(tipo, new BigDecimal(monto), alias, causa);
        d.setFecha(LocalDateTime.now().plusDays(diasOffset));
        return d;
    }
}
