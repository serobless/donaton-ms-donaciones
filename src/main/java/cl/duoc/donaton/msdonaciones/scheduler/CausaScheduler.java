package cl.duoc.donaton.msdonaciones.scheduler;

import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CausaScheduler {

    private final CausaRepository causaRepository;

    /**
     * Corre cada día a medianoche.
     * Cierra automáticamente causas vencidas (fechaFin < hoy) o completadas (recaudado >= meta).
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void cerrarCausasInactivas() {
        int vencidas    = causaRepository.cerrarVencidas(LocalDate.now());
        int completadas = causaRepository.cerrarCompletadas();
        if (vencidas > 0 || completadas > 0) {
            log.info("CausaScheduler: {} causa(s) vencida(s) cerrada(s), {} causa(s) completada(s) cerrada(s).",
                     vencidas, completadas);
        }
    }
}
