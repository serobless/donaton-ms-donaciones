package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CausaRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CausaService {

    private final CausaRepository causaRepository;

    public List<Causa> listarActivas() {
        return causaRepository.findByActivaTrue();
    }

    public Causa obtenerPorId(Long id) {
        return causaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Causa no encontrada con id: " + id));
    }

    @Transactional
    public Causa crear(CausaRequest req) {
        Causa causa = Causa.builder()
                .titulo(req.getTitulo())
                .descripcion(req.getDescripcion())
                .meta(req.getMeta())
                .categoria(req.getCategoria())
                .imagenUrl(req.getImagenUrl())
                .diasRestantes(req.getDiasRestantes())
                .fechaInicio(req.getFechaInicio())
                .build();
        return causaRepository.save(causa);
    }

    @Transactional
    public Causa actualizar(Long id, CausaRequest req) {
        Causa c = obtenerPorId(id);
        c.setTitulo(req.getTitulo());
        c.setDescripcion(req.getDescripcion());
        c.setMeta(req.getMeta());
        c.setCategoria(req.getCategoria());
        c.setImagenUrl(req.getImagenUrl());
        c.setDiasRestantes(req.getDiasRestantes());
        if (req.getDestacada() != null) c.setDestacada(req.getDestacada());
        c.setUrgencia(req.getUrgencia());
        c.setFechaInicio(req.getFechaInicio());
        return causaRepository.save(c);
    }

    @Transactional
    public void eliminar(Long id) {
        causaRepository.deleteById(id);
    }

    // Actualiza el recaudado tras cada donación monetaria
    @Transactional
    public void actualizarRecaudado(Causa causa, java.math.BigDecimal monto) {
        causa.setRecaudado(causa.getRecaudado().add(monto));
        causaRepository.save(causa);
    }
}
