package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CausaRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.repository.CausaRepository;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import cl.duoc.donaton.msdonaciones.repository.ItemDonacionRepository;
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
    private final CentroAcopioRepository centroAcopioRepository;
    private final DonacionRepository donacionRepository;
    private final ItemDonacionRepository itemDonacionRepository;

    public List<Causa> listarTodas() {
        return causaRepository.findAll();
    }

    public List<Causa> listarActivas() {
        return causaRepository.findByActivaTrue();
    }

    public Causa obtenerPorId(Long id) {
        return causaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Causa no encontrada con id: " + id));
    }

    @Transactional
    public Causa crear(CausaRequest req) {
        CentroAcopio centro = req.getCentroId() != null
                ? centroAcopioRepository.findById(req.getCentroId()).orElse(null)
                : null;
        Causa causa = Causa.builder()
                .titulo(req.getTitulo())
                .descripcion(req.getDescripcion())
                .meta(req.getMeta())
                .categoria(req.getCategoria())
                .tipo(req.getTipo())
                .imagenUrl(req.getImagenUrl())
                .diasRestantes(req.getDiasRestantes())
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .centro(centro)
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
        c.setTipo(req.getTipo());
        if (req.getImagenUrl() != null) c.setImagenUrl(req.getImagenUrl());
        c.setDiasRestantes(req.getDiasRestantes());
        if (req.getDestacada() != null) c.setDestacada(req.getDestacada());
        c.setUrgencia(req.getUrgencia());
        c.setFechaInicio(req.getFechaInicio());
        c.setFechaFin(req.getFechaFin());
        CentroAcopio centro = req.getCentroId() != null
                ? centroAcopioRepository.findById(req.getCentroId()).orElse(null)
                : null;
        c.setCentro(centro);
        return causaRepository.save(c);
    }

    @Transactional
    public Causa toggleDestacada(Long id) {
        boolean esDestacada = Boolean.TRUE.equals(obtenerPorId(id).getDestacada());
        boolean nuevoValor  = !esDestacada;
        // Siempre limpia primero (clearAutomatically invalida el cache de sesión)
        causaRepository.desmarcarTodasDestacadas();
        if (nuevoValor) {
            // Solo una puede estar activa; la recargamos para tener el estado limpio
            Causa c = obtenerPorId(id);
            c.setDestacada(true);
            return causaRepository.save(c);
        }
        // Si estaba activa y se desactiva, ya quedó en false por el bulk update
        return obtenerPorId(id);
    }

    @Transactional
    public Causa toggleActiva(Long id) {
        Causa c = obtenerPorId(id);
        c.setActiva(!Boolean.TRUE.equals(c.getActiva()));
        return causaRepository.save(c);
    }

    @Transactional
    public void eliminar(Long id) {
        List<Donacion> donaciones = donacionRepository.findByCausaId(id);
        if (!donaciones.isEmpty()) {
            itemDonacionRepository.deleteByDonacionIn(donaciones);
            donacionRepository.deleteAll(donaciones);
        }
        causaRepository.deleteById(id);
    }

    // Actualiza el recaudado tras cada donación monetaria
    @Transactional
    public void actualizarRecaudado(Causa causa, java.math.BigDecimal monto) {
        causa.setRecaudado(causa.getRecaudado().add(monto));
        causaRepository.save(causa);
    }
}
