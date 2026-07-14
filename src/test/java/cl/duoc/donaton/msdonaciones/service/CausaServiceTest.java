package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CausaRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CausaServiceTest {

    @Mock
    private CausaRepository causaRepository;

    @Mock
    private CentroAcopioRepository centroAcopioRepository;

    @InjectMocks
    private CausaService causaService;

    private CausaRequest requestDummy() {
        CausaRequest req = new CausaRequest();
        req.setTitulo("Abrigo para invierno 2026");
        req.setDescripcion("Recolección de ropa de abrigo para personas en situación de calle.");
        req.setMeta(BigDecimal.valueOf(1500000));
        req.setCategoria("VESTIMENTA");
        req.setFechaInicio(LocalDate.now());
        req.setFechaFin(LocalDate.now().plusMonths(3));
        return req;
    }

    // ── Crear causa ────────────────────────────────────────────────────────────

    @Test
    void crearCausa_valida_retornaCausaActiva() {
        CausaRequest req = requestDummy();
        when(causaRepository.save(any(Causa.class))).thenAnswer(inv -> {
            Causa c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Causa resultado = causaService.crear(req);

        assertNotNull(resultado);
        assertEquals("Abrigo para invierno 2026", resultado.getTitulo());
        assertEquals(BigDecimal.valueOf(1500000), resultado.getMeta());
        assertTrue(resultado.getActiva());
        verify(causaRepository).save(any(Causa.class));
    }

    // ── Obtener causa por ID ───────────────────────────────────────────────────

    @Test
    void obtenerCausa_existente_retornaCausa() {
        Causa causa = Causa.builder()
                .id(1L)
                .titulo("Alimentación para familias vulnerables")
                .meta(BigDecimal.valueOf(5000000))
                .activa(true)
                .build();
        when(causaRepository.findById(1L)).thenReturn(Optional.of(causa));

        Causa resultado = causaService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Alimentación para familias vulnerables", resultado.getTitulo());
    }

    @Test
    void obtenerCausa_noExiste_lanzaEntityNotFoundException() {
        when(causaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> causaService.obtenerPorId(99L));
    }

    // ── Listar causas ──────────────────────────────────────────────────────────

    @Test
    void listarActivas_retornaSoloCausasActivas() {
        List<Causa> causas = List.of(
                Causa.builder().id(1L).titulo("Causa Activa A").activa(true)
                        .meta(BigDecimal.valueOf(1000000)).build(),
                Causa.builder().id(2L).titulo("Causa Activa B").activa(true)
                        .meta(BigDecimal.valueOf(2000000)).build()
        );
        when(causaRepository.findByActivaTrue()).thenReturn(causas);

        List<Causa> resultado = causaService.listarActivas();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(Causa::getActiva));
        verify(causaRepository).findByActivaTrue();
    }

    // ── Actualizar recaudado ───────────────────────────────────────────────────

    @Test
    void actualizarRecaudado_sumaMonto_correctamente() {
        Causa causa = Causa.builder()
                .id(1L)
                .titulo("Medicamentos para adultos mayores")
                .meta(BigDecimal.valueOf(2000000))
                .recaudado(BigDecimal.valueOf(500000))
                .activa(true)
                .build();
        when(causaRepository.save(any(Causa.class))).thenAnswer(inv -> inv.getArgument(0));

        causaService.actualizarRecaudado(causa, BigDecimal.valueOf(200000));

        assertEquals(0, BigDecimal.valueOf(700000).compareTo(causa.getRecaudado()));
        verify(causaRepository).save(causa);
    }

    @Test
    void actualizarRecaudado_alcanzaMeta_causaSigueSiendoPersistida() {
        Causa causa = Causa.builder()
                .id(2L)
                .titulo("Causa casi completa")
                .meta(BigDecimal.valueOf(1000000))
                .recaudado(BigDecimal.valueOf(900000))
                .activa(true)
                .build();
        when(causaRepository.save(any(Causa.class))).thenAnswer(inv -> inv.getArgument(0));

        causaService.actualizarRecaudado(causa, BigDecimal.valueOf(200000));

        // El recaudado supera la meta (sobre-financiamiento permitido)
        assertEquals(0, BigDecimal.valueOf(1100000).compareTo(causa.getRecaudado()));
        verify(causaRepository).save(causa);
    }
}
