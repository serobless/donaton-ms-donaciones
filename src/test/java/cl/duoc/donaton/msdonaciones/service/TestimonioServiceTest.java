package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.TestimonioRequest;
import cl.duoc.donaton.msdonaciones.model.Testimonio;
import cl.duoc.donaton.msdonaciones.repository.TestimonioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestimonioServiceTest {

    @Mock
    private TestimonioRepository testimonioRepository;

    @InjectMocks
    private TestimonioService testimonioService;

    // ── Crear testimonio ───────────────────────────────────────────────────────

    @Test
    void crearTestimonio_valido_quedaPendienteDeAprobacion() {
        TestimonioRequest req = new TestimonioRequest();
        req.setTitulo("Mi experiencia donando ropa este invierno");
        req.setContenido("<p>Fue una experiencia increíble donar en Donaton.</p>");

        when(testimonioRepository.save(any(Testimonio.class))).thenAnswer(inv -> {
            Testimonio t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Testimonio resultado = testimonioService.crear(req, "user-42", "Carlos Miranda");

        assertNotNull(resultado);
        assertEquals("Mi experiencia donando ropa este invierno", resultado.getTitulo());
        assertEquals("user-42", resultado.getAutorId());
        assertEquals("Carlos Miranda", resultado.getAutorNombre());
        // Todo testimonio nuevo debe estar pendiente de aprobación
        assertFalse(Boolean.TRUE.equals(resultado.getAprobado()));
        assertNotNull(resultado.getFechaCreacion());
        verify(testimonioRepository).save(any(Testimonio.class));
    }

    // ── Listar solo aprobados (vista pública) ──────────────────────────────────

    @Test
    void listar_retornaSoloAprobados() {
        List<Testimonio> aprobados = List.of(
                Testimonio.builder().id(1L).titulo("Testimonio aprobado A")
                        .aprobado(true).build(),
                Testimonio.builder().id(2L).titulo("Testimonio aprobado B")
                        .aprobado(true).build()
        );
        // listar() debe llamar a findByAprobadoTrue(), no findAll()
        when(testimonioRepository.findByAprobadoTrue()).thenReturn(aprobados);

        List<Testimonio> resultado = testimonioService.listar();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(t -> Boolean.TRUE.equals(t.getAprobado())));
        verify(testimonioRepository).findByAprobadoTrue();
        verify(testimonioRepository, never()).findAll();
    }

    // ── Listar pendientes de aprobación (vista ADMIN) ──────────────────────────

    @Test
    void listarPendientes_retornaNoAprobados() {
        List<Testimonio> pendientes = List.of(
                Testimonio.builder().id(3L).titulo("Nuevo testimonio sin revisar")
                        .aprobado(false).build()
        );
        when(testimonioRepository.findByAprobadoFalse()).thenReturn(pendientes);

        List<Testimonio> resultado = testimonioService.listarPendientes();

        assertEquals(1, resultado.size());
        assertFalse(Boolean.TRUE.equals(resultado.get(0).getAprobado()));
        verify(testimonioRepository).findByAprobadoFalse();
    }

    // ── Flujo de aprobación ────────────────────────────────────────────────────

    @Test
    void aprobar_testimonioExistente_marcaComoAprobado() {
        Testimonio pendiente = Testimonio.builder()
                .id(5L)
                .titulo("Testimonio pendiente")
                .aprobado(false)
                .build();

        when(testimonioRepository.findById(5L)).thenReturn(Optional.of(pendiente));
        when(testimonioRepository.save(any(Testimonio.class))).thenAnswer(inv -> inv.getArgument(0));

        Testimonio resultado = testimonioService.aprobar(5L);

        assertTrue(Boolean.TRUE.equals(resultado.getAprobado()));
        verify(testimonioRepository).save(pendiente);
    }

    @Test
    void aprobar_testimonioNoExiste_lanzaEntityNotFoundException() {
        when(testimonioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> testimonioService.aprobar(99L));
        verify(testimonioRepository, never()).save(any());
    }

    // ── Eliminar testimonio ────────────────────────────────────────────────────

    @Test
    void eliminar_testimonioExistente_eliminaCorrectamente() {
        when(testimonioRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> testimonioService.eliminar(1L));
        verify(testimonioRepository).deleteById(1L);
    }

    @Test
    void eliminar_testimonioNoExiste_lanzaEntityNotFoundException() {
        when(testimonioRepository.existsById(99L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> testimonioService.eliminar(99L));
        verify(testimonioRepository, never()).deleteById(any());
    }
}
