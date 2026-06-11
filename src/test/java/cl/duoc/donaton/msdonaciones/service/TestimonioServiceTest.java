package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.TestimonioRequest;
import cl.duoc.donaton.msdonaciones.model.Testimonio;
import cl.duoc.donaton.msdonaciones.repository.TestimonioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestimonioServiceTest {

    @Mock
    private TestimonioRepository testimonioRepository;

    @InjectMocks
    private TestimonioService testimonioService;

    @Test
    void testCrearTestimonio_valido_retornaTestimonio() {
        TestimonioRequest req = new TestimonioRequest();
        req.setTitulo("Mi experiencia donando");
        req.setContenido("<p>Fue una experiencia increíble.</p>");
        req.setImagenUrl("https://example.com/img.jpg");

        when(testimonioRepository.save(any(Testimonio.class))).thenAnswer(inv -> {
            Testimonio t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        Testimonio resultado = testimonioService.crear(req, "user-42", "Carlos Miranda");

        assertNotNull(resultado);
        assertEquals("Mi experiencia donando", resultado.getTitulo());
        assertEquals("user-42", resultado.getAutorId());
        assertEquals("Carlos Miranda", resultado.getAutorNombre());
        assertNotNull(resultado.getFechaCreacion());
        verify(testimonioRepository).save(any(Testimonio.class));
    }

    @Test
    void testListarTestimonios_retornaLista() {
        List<Testimonio> testimonios = List.of(
                Testimonio.builder().id(1L).titulo("Testimonio 1").contenido("Contenido 1").build(),
                Testimonio.builder().id(2L).titulo("Testimonio 2").contenido("Contenido 2").build()
        );
        when(testimonioRepository.findAll()).thenReturn(testimonios);

        List<Testimonio> resultado = testimonioService.listar();

        assertEquals(2, resultado.size());
        verify(testimonioRepository).findAll();
    }
}
