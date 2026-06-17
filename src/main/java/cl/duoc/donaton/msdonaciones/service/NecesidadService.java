package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.NecesidadRequest;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.model.Necesidad;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import cl.duoc.donaton.msdonaciones.repository.NecesidadRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NecesidadService {

    private final NecesidadRepository necesidadRepo;
    private final CentroAcopioRepository centroRepo;

    public List<Necesidad> listarTodas() { return necesidadRepo.findByActivaTrue(); }

    public List<Necesidad> listarPorCentro(Long centroId) {
        return necesidadRepo.findByCentroIdAndActivaTrue(centroId);
    }

    @Transactional
    public Necesidad crear(Long centroId, NecesidadRequest req) {
        CentroAcopio centro = centroRepo.findById(centroId)
            .orElseThrow(() -> new EntityNotFoundException("Centro no encontrado: " + centroId));
        return necesidadRepo.save(Necesidad.builder()
            .centro(centro).tipo(req.getTipo()).descripcion(req.getDescripcion())
            .metaUnidades(req.getMetaUnidades())
            .unidadesActuales(req.getUnidadesActuales() != null ? req.getUnidadesActuales() : 0)
            .urgente(req.getUrgente() != null && req.getUrgente())
            .diasRestantes(req.getDiasRestantes())
            .build());
    }

    @Transactional
    public Necesidad actualizar(Long id, NecesidadRequest req) {
        Necesidad n = necesidadRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Necesidad no encontrada: " + id));
        n.setTipo(req.getTipo()); n.setDescripcion(req.getDescripcion());
        n.setMetaUnidades(req.getMetaUnidades());
        if (req.getUnidadesActuales() != null) n.setUnidadesActuales(req.getUnidadesActuales());
        if (req.getUrgente() != null) n.setUrgente(req.getUrgente());
        n.setDiasRestantes(req.getDiasRestantes());
        return necesidadRepo.save(n);
    }

    @Transactional
    public void eliminar(Long id) { necesidadRepo.deleteById(id); }
}
