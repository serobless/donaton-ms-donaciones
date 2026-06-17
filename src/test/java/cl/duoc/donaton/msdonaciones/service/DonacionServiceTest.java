package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.DonacionRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.EstadoDonacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock
    private DonacionRepository donacionRepository;

    @Mock
    private CausaService causaService;

    @InjectMocks
    private DonacionService donacionService;

    private Causa causaDummy() {
        return Causa.builder()
                .id(1L)
                .titulo("Causa Test")
                .meta(BigDecimal.valueOf(1000))
                .build();
    }

    @Test
    void testCrearDonacion_valida_retornaDonacion() {
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(1L);
        req.setTipoDonacion(TipoDonacion.MONETARIA);
        req.setMonto(BigDecimal.valueOf(5000));
        req.setDonanteAlias("Carlos");

        Causa causa = causaDummy();
        when(causaService.obtenerPorId(1L)).thenReturn(causa);
        when(donacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Donacion resultado = donacionService.crear(req, "user-1");

        assertNotNull(resultado);
        assertEquals(TipoDonacion.MONETARIA, resultado.getTipoDonacion());
        assertEquals(EstadoDonacion.PENDIENTE, resultado.getEstado());
        verify(causaService).actualizarRecaudado(any(Causa.class), any(BigDecimal.class));
    }

    @Test
    void testCrearDonacion_causaNoExiste_lanzaException() {
        DonacionRequest req = new DonacionRequest();
        req.setCausaId(99L);
        req.setTipoDonacion(TipoDonacion.MONETARIA);
        req.setMonto(BigDecimal.valueOf(1000));

        when(causaService.obtenerPorId(99L))
                .thenThrow(new EntityNotFoundException("Causa no encontrada con id: 99"));

        assertThrows(EntityNotFoundException.class, () -> donacionService.crear(req, "user-1"));
    }

    @Test
    void testActualizarEstado_donacionExiste_actualizaEstado() {
        Donacion donacion = Donacion.builder()
                .id(1L)
                .tipoDonacion(TipoDonacion.MONETARIA)
                .monto(BigDecimal.valueOf(5000))
                .causa(causaDummy())
                .build();

        when(donacionRepository.findById(1L)).thenReturn(Optional.of(donacion));
        when(donacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Donacion resultado = donacionService.actualizarEstado(1L, EstadoDonacion.COMPLETADA);

        assertEquals(EstadoDonacion.COMPLETADA, resultado.getEstado());
        verify(donacionRepository).save(donacion);
    }

    @Test
    void testActualizarEstado_donacionNoExiste_lanza404() {
        when(donacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> donacionService.actualizarEstado(99L, EstadoDonacion.COMPLETADA));
    }

    @Test
    void testListarDonaciones_retornaLista() {
        List<Donacion> donaciones = List.of(
                Donacion.builder().id(1L).tipoDonacion(TipoDonacion.MONETARIA)
                        .monto(BigDecimal.valueOf(1000)).causa(causaDummy()).build(),
                Donacion.builder().id(2L).tipoDonacion(TipoDonacion.ROPA)
                        .monto(BigDecimal.ZERO).causa(causaDummy()).build()
        );
        when(donacionRepository.findAll()).thenReturn(donaciones);

        List<Donacion> resultado = donacionService.listarTodas();

        assertEquals(2, resultado.size());
        verify(donacionRepository).findAll();
    }
}
