package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.DonacionRequest;
import cl.duoc.donaton.msdonaciones.dto.TopDonadorResponse;
import cl.duoc.donaton.msdonaciones.dto.TransparenciaResponse;
import cl.duoc.donaton.msdonaciones.factory.DonacionFactory;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.model.EstadoDonacion;
import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import cl.duoc.donaton.msdonaciones.repository.DonacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final CausaService causaService;
    private final CentroAcopioRepository centroAcopioRepository;

    public List<Donacion> listarTodas() {
        return donacionRepository.findAll();
    }

    @Transactional
    public Donacion crear(DonacionRequest req, String donadorId) {
        Causa causa = causaService.obtenerPorId(req.getCausaId());

        Donacion donacion = DonacionFactory.crear(
                req.getTipoDonacion(),
                req.getMonto() != null ? req.getMonto() : BigDecimal.ZERO,
                req.getDonanteAlias(),
                causa
        );
        donacion.setDonadorId(donadorId);
        donacion.setDescripcion(req.getDescripcion());
        donacion.setCantidad(req.getCantidad());
        donacion.setUnidad(req.getUnidad());

        if (req.getCentroAcopioId() != null) {
            centroAcopioRepository.findById(req.getCentroAcopioId()).ifPresent(donacion::setCentroAcopio);
            // Sumar ítems físicos a capacidadActual via UPDATE directo (evita merge sobre @ElementCollection lazy)
            if (req.getTipoDonacion() != TipoDonacion.MONETARIA
                    && req.getCantidad() != null && req.getCantidad() > 0) {
                centroAcopioRepository.incrementarCapacidad(req.getCentroAcopioId(), req.getCantidad());
            }
        }

        Donacion guardada = donacionRepository.save(donacion);

        // Solo las donaciones monetarias suman al recaudado
        if (req.getTipoDonacion() == TipoDonacion.MONETARIA && req.getMonto() != null) {
            causaService.actualizarRecaudado(causa, req.getMonto());
        }

        return guardada;
    }

    public List<TopDonadorResponse> topDonadores() {
        return donacionRepository.findTop10Donantes().stream()
                .map(row -> new TopDonadorResponse(
                        (String) row[0],
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    public List<Donacion> listarPorDonador(String donadorId) {
        if (donadorId == null || donadorId.isBlank()) return List.of();
        return donacionRepository.findByDonadorId(donadorId);
    }

    public List<Donacion> listarUltimas(int limit) {
        return donacionRepository.findAllByOrderByFechaDesc(PageRequest.of(0, limit));
    }

    public BigDecimal totalRecaudado() {
        return donacionRepository.sumTotalMontos();
    }

    public long conteo() {
        return donacionRepository.count();
    }

    @Transactional
    public Donacion actualizarEstado(Long id, EstadoDonacion nuevoEstado) {
        Donacion donacion = donacionRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Donación no encontrada: " + id));
        donacion.setEstado(nuevoEstado);
        return donacionRepository.save(donacion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!donacionRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Donación no encontrada: " + id);
        }
        donacionRepository.deleteById(id);
    }

    public List<TransparenciaResponse> transparencia() {
        return donacionRepository.findAll().stream()
                .map(d -> TransparenciaResponse.builder()
                        .id(d.getId())
                        .donadorNombre(d.getDonanteAlias() != null ? d.getDonanteAlias() : "Anónimo")
                        .monto(d.getMonto())
                        .tipoDonacion(d.getTipoDonacion())
                        .fecha(d.getFecha())
                        .causaNombre(d.getCausa().getTitulo())
                        .descripcion(d.getDescripcion())
                        .build()
                )
                .toList();
    }
}
