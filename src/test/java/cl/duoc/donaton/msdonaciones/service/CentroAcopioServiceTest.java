package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CentroAcopioRequest;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CentroAcopioServiceTest {

    @Mock
    private CentroAcopioRepository repo;

    @InjectMocks
    private CentroAcopioService centroAcopioService;

    private CentroAcopioRequest requestDummy() {
        CentroAcopioRequest req = new CentroAcopioRequest();
        req.setNombre("Centro Santiago Sur");
        req.setDireccion("Av. Vicuña Mackenna 1234");
        req.setCiudad("Santiago");
        req.setRegion("Metropolitana");
        req.setHorario("Lun-Vie 9:00-18:00");
        req.setTelefono("+56 2 2345 9000");
        req.setQueRecibe(List.of("Ropa de abrigo", "Frazadas"));
        req.setCapacidadActual(0);
        req.setCapacidadMax(300);
        req.setUnidadCapacidad("dm³");
        req.setLatitud(-33.4891);
        req.setLongitud(-70.6432);
        return req;
    }

    // ── Crear centro válido ────────────────────────────────────────────────────

    @Test
    void crearCentro_valido_retornaCentroGuardado() {
        when(repo.existsByDireccionIgnoreCaseAndCiudadIgnoreCase(anyString(), anyString()))
                .thenReturn(false);
        when(repo.existsByCoordenadasCercanas(anyDouble(), anyDouble()))
                .thenReturn(false);
        when(repo.save(any(CentroAcopio.class))).thenAnswer(inv -> {
            CentroAcopio c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CentroAcopio resultado = centroAcopioService.crear(requestDummy());

        assertNotNull(resultado);
        assertEquals("Centro Santiago Sur", resultado.getNombre());
        assertEquals("Santiago", resultado.getCiudad());
        verify(repo).save(any(CentroAcopio.class));
    }

    // ── Dirección duplicada al crear ───────────────────────────────────────────

    @Test
    void crearCentro_direccionDuplicada_lanzaExcepcion() {
        when(repo.existsByDireccionIgnoreCaseAndCiudadIgnoreCase(
                "Av. Vicuña Mackenna 1234", "Santiago"))
                .thenReturn(true);

        CentroAcopioRequest req = requestDummy();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> centroAcopioService.crear(req));

        assertTrue(ex.getMessage().contains("Av. Vicuña Mackenna 1234"));
        verify(repo, never()).save(any());
    }

    // ── Dirección duplicada al editar (otro centro, misma dirección) ───────────

    @Test
    void actualizarCentro_direccionUsadaPorOtroCentro_lanzaExcepcion() {
        CentroAcopio existente = CentroAcopio.builder()
                .id(5L).nombre("Centro Santiago Sur").ciudad("Santiago")
                .direccion("Av. Vicuña Mackenna 1234").build();

        when(repo.findById(5L)).thenReturn(Optional.of(existente));
        when(repo.existsByDireccionIgnoreCaseAndCiudadIgnoreCaseAndIdNot(
                "Av. Brasil 678", "Valparaíso", 5L))
                .thenReturn(true);

        CentroAcopioRequest req = requestDummy();
        req.setDireccion("Av. Brasil 678");
        req.setCiudad("Valparaíso");

        assertThrows(IllegalArgumentException.class,
                () -> centroAcopioService.actualizar(5L, req));
        verify(repo, never()).save(any());
    }

    // ── Editar el propio centro sin cambiar dirección no falla ─────────────────

    @Test
    void actualizarCentro_mismadireccionPropioCentro_guardaCorrectamente() {
        CentroAcopio existente = CentroAcopio.builder()
                .id(5L).nombre("Centro Santiago Sur").ciudad("Santiago")
                .direccion("Av. Vicuña Mackenna 1234")
                .queRecibe(new java.util.ArrayList<>())
                .build();

        when(repo.findById(5L)).thenReturn(Optional.of(existente));
        // El propio centro no se cuenta como duplicado (excludes id=5)
        when(repo.existsByDireccionIgnoreCaseAndCiudadIgnoreCaseAndIdNot(
                "Av. Vicuña Mackenna 1234", "Santiago", 5L))
                .thenReturn(false);
        when(repo.existsByCoordenadasCercanasAndIdNot(anyDouble(), anyDouble(), eq(5L)))
                .thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CentroAcopioRequest req = requestDummy();
        req.setNombre("Centro Santiago Sur — actualizado");

        CentroAcopio resultado = centroAcopioService.actualizar(5L, req);

        assertEquals("Centro Santiago Sur — actualizado", resultado.getNombre());
    }

    // ── Centro no encontrado ───────────────────────────────────────────────────

    @Test
    void actualizarCentro_noExiste_lanzaEntityNotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> centroAcopioService.actualizar(99L, requestDummy()));
    }

    // ── Listar centros activos ─────────────────────────────────────────────────

    @Test
    void listar_retornaSoloCentrosActivos() {
        List<CentroAcopio> activos = List.of(
                CentroAcopio.builder().id(1L).nombre("Casa Central").activo(true).build(),
                CentroAcopio.builder().id(2L).nombre("Centro Valparaíso").activo(true).build()
        );
        when(repo.findByActivoTrue()).thenReturn(activos);

        List<CentroAcopio> resultado = centroAcopioService.listar();

        assertEquals(2, resultado.size());
        verify(repo).findByActivoTrue();
    }

    // ── Capacidad negativa no permitida ───────────────────────────────────────

    @Test
    void actualizarCapacidad_valorNegativo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> centroAcopioService.actualizarCapacidad(1L, -10, null));
        verify(repo, never()).save(any());
    }
}
