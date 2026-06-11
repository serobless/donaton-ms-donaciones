package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CausaRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
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
class CausaServiceTest {

    @Mock
    private CausaRepository causaRepository;

    @InjectMocks
    private CausaService causaService;

    private CausaRequest requestDummy() {
        CausaRequest req = new CausaRequest();
        req.setNombre("Alimentando Esperanzas");
        req.setDescripcion("Ayuda a familias vulnerables");
        req.setMeta(BigDecimal.valueOf(500000));
        req.setCategoria("ALIMENTACION");
        return req;
    }

    @Test
    void testCrearCausa_valida_retornaCausa() {
        CausaRequest req = requestDummy();
        when(causaRepository.save(any(Causa.class))).thenAnswer(inv -> {
            Causa c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Causa resultado = causaService.crear(req);

        assertNotNull(resultado);
        assertEquals("Alimentando Esperanzas", resultado.getNombre());
        assertEquals(BigDecimal.valueOf(500000), resultado.getMeta());
        verify(causaRepository).save(any(Causa.class));
    }

    @Test
    void testObtenerCausa_existente_retornaCausa() {
        Causa causa = Causa.builder()
                .id(1L)
                .nombre("Causa Existente")
                .meta(BigDecimal.valueOf(100000))
                .build();
        when(causaRepository.findById(1L)).thenReturn(Optional.of(causa));

        Causa resultado = causaService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Causa Existente", resultado.getNombre());
    }

    @Test
    void testObtenerCausa_noExiste_lanza404() {
        when(causaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> causaService.obtenerPorId(99L));
    }

    @Test
    void testListarCausas_retornaLista() {
        List<Causa> causas = List.of(
                Causa.builder().id(1L).nombre("Causa A").meta(BigDecimal.valueOf(1000)).build(),
                Causa.builder().id(2L).nombre("Causa B").meta(BigDecimal.valueOf(2000)).build()
        );
        when(causaRepository.findByActivaTrue()).thenReturn(causas);

        List<Causa> resultado = causaService.listarActivas();

        assertEquals(2, resultado.size());
        verify(causaRepository).findByActivaTrue();
    }
}
