package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.DonacionRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.EstadoDonacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import cl.duoc.donaton.msdonaciones.repository.ItemDonacionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock private DonacionRepository donacionRepository;
    @Mock private ItemDonacionRepository itemDonacionRepository;
    @Mock private CausaService causaService;
    @Mock private CausaRepository causaRepository;
    @Mock private CentroAcopioRepository centroAcopioRepository;

    @InjectMocks
    private DonacionService donacionService;

    private Causa causaDummy() {
        return Causa.builder()
                .id(1L)
                .titulo("Alimentación para familias vulnerables")
                .meta(BigDecimal.valueOf(5000000))
                .recaudado(BigDecimal.ZERO)
                .activa(true)
                .build();
    }

    private void stubCasaCentral() {
        CentroAcopio casaCentral = CentroAcopio.builder()
                .id(1L).nombre("Casa Central").ciudad("Santiago").build();
        when(centroAcopioRepository.findByNombre("Casa Central"))
                .thenReturn(Optional.of(casaCentral));
    }

    // ── Donación monetaria válida ──────────────────────────────────────────────

    @Test
    void crearDonacionMonetaria_valida_retornaDonacion() {
        stubCasaCentral();
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(1L);
        req.setTipoDonacion(TipoDonacion.MONETARIA);
        req.setMonto(BigDecimal.valueOf(50000));
        req.setDonanteAlias("María González");

        when(causaService.obtenerPorId(1L)).thenReturn(causaDummy());
        when(donacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Donacion resultado = donacionService.crear(req, "user-1");

        assertNotNull(resultado);
        assertEquals(TipoDonacion.MONETARIA, resultado.getTipoDonacion());
        assertEquals(EstadoDonacion.PENDIENTE, resultado.getEstado());
        assertFalse(Boolean.TRUE.equals(resultado.getRequiereAprobacion()));
        verify(causaService).actualizarRecaudado(any(Causa.class), any(BigDecimal.class));
    }

    // ── Causa inexistente lanza 404 ────────────────────────────────────────────

    @Test
    void crearDonacion_causaNoExiste_lanzaEntityNotFoundException() {
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(99L);
        req.setTipoDonacion(TipoDonacion.MONETARIA);
        req.setMonto(BigDecimal.valueOf(10000));

        when(causaService.obtenerPorId(99L))
                .thenThrow(new EntityNotFoundException("Causa no encontrada con id: 99"));

        assertThrows(EntityNotFoundException.class, () -> donacionService.crear(req, "user-1"));
        verify(donacionRepository, never()).save(any());
    }

    // ── Límite $3.000.000 para donantes normales ───────────────────────────────

    @Test
    void crearDonacionMonetaria_superaLimite_noEmpresa_lanzaExcepcion() {
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(1L);
        req.setTipoDonacion(TipoDonacion.MONETARIA);
        req.setMonto(BigDecimal.valueOf(5000000));
        req.setEsEmpresa(false);

        // La excepción se lanza antes de consultar la causa, no se necesita stub
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> donacionService.crear(req, "user-1"));
        assertTrue(ex.getMessage().contains("3.000.000"));
        verify(donacionRepository, never()).save(any());
    }

    // ── Empresa > $3M queda pendiente de aprobación ───────────────────────────

    @Test
    void crearDonacionMonetaria_empresa_superaLimite_requiereAprobacion() {
        stubCasaCentral();
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(1L);
        req.setTipoDonacion(TipoDonacion.MONETARIA);
        req.setMonto(BigDecimal.valueOf(5000000));
        req.setEsEmpresa(true);
        req.setNombreEmpresa("Empresa S.A.");

        when(causaService.obtenerPorId(1L)).thenReturn(causaDummy());
        when(donacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Donacion resultado = donacionService.crear(req, "user-empresa");

        assertTrue(Boolean.TRUE.equals(resultado.getRequiereAprobacion()));
        // No debe sumar al recaudado hasta ser aprobada
        verify(causaService, never()).actualizarRecaudado(any(), any());
    }

    // ── Donación en especie (ropa) sin monto estimado ─────────────────────────

    @Test
    void crearDonacionRopa_sinMontoEstimado_seGuardaConCero() {
        stubCasaCentral();
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(1L);
        req.setTipoDonacion(TipoDonacion.ROPA);
        req.setMonto(BigDecimal.ZERO);
        req.setCantidad(5);
        req.setUnidad("prendas");
        req.setDonanteAlias("Anónimo");

        when(causaService.obtenerPorId(1L)).thenReturn(causaDummy());
        when(donacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Donacion resultado = donacionService.crear(req, "user-2");

        assertEquals(TipoDonacion.ROPA, resultado.getTipoDonacion());
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.getMonto()));
        // Las donaciones en especie no suman al recaudado monetario
        verify(causaService, never()).actualizarRecaudado(any(), any());
    }

    // ── Cambio de estado ───────────────────────────────────────────────────────

    @Test
    void actualizarEstado_donacionExiste_cambiasEstado() {
        Donacion donacion = Donacion.builder()
                .id(1L)
                .tipoDonacion(TipoDonacion.MONETARIA)
                .monto(BigDecimal.valueOf(50000))
                .estado(EstadoDonacion.PENDIENTE)
                .causa(causaDummy())
                .build();

        when(donacionRepository.findById(1L)).thenReturn(Optional.of(donacion));
        when(donacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Donacion resultado = donacionService.actualizarEstado(1L, EstadoDonacion.EN_PROCESO);

        assertEquals(EstadoDonacion.EN_PROCESO, resultado.getEstado());
        verify(donacionRepository).save(donacion);
    }

    @Test
    void actualizarEstado_donacionNoExiste_lanza404() {
        when(donacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> donacionService.actualizarEstado(99L, EstadoDonacion.COMPLETADA));
    }

    // ── Listar donaciones ──────────────────────────────────────────────────────

    @Test
    void listarTodas_retornaListaCompleta() {
        List<Donacion> donaciones = List.of(
                Donacion.builder().id(1L).tipoDonacion(TipoDonacion.MONETARIA)
                        .monto(BigDecimal.valueOf(50000)).causa(causaDummy()).build(),
                Donacion.builder().id(2L).tipoDonacion(TipoDonacion.ROPA)
                        .monto(BigDecimal.ZERO).causa(causaDummy()).build()
        );
        when(donacionRepository.findAll()).thenReturn(donaciones);

        List<Donacion> resultado = donacionService.listarTodas();

        assertEquals(2, resultado.size());
        verify(donacionRepository).findAll();
    }
}
