package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.TestimonioRequest;
import cl.duoc.donaton.msdonaciones.model.Testimonio;
import cl.duoc.donaton.msdonaciones.repository.TestimonioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestimonioService {

    private final TestimonioRepository testimonioRepository;

    public List<Testimonio> listar() {
        return testimonioRepository.findAll();
    }

    @Transactional
    public Testimonio crear(TestimonioRequest req, String autorId, String autorNombre) {
        Testimonio testimonio = Testimonio.builder()
                .titulo(req.getTitulo())
                .contenido(req.getContenido())
                .imagenUrl(req.getImagenUrl())
                .autorId(autorId)
                .autorNombre(autorNombre)
                .build();
        return testimonioRepository.save(testimonio);
    }
}
