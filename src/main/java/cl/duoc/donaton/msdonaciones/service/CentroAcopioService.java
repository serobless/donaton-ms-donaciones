package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CentroAcopioRequest;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CentroAcopioService {

    private final CentroAcopioRepository repo;

    public List<CentroAcopio> listar() { return repo.findByActivoTrue(); }

    public CentroAcopio obtenerPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Centro no encontrado: " + id));
    }

    @Transactional
    public CentroAcopio crear(CentroAcopioRequest req) {
        return repo.save(CentroAcopio.builder()
            .nombre(req.getNombre()).direccion(req.getDireccion()).region(req.getRegion())
            .ciudad(req.getCiudad()).horario(req.getHorario()).telefono(req.getTelefono())
            .queRecibe(req.getQueRecibe()).capacidadActual(req.getCapacidadActual())
            .capacidadMax(req.getCapacidadMax()).latitud(req.getLatitud()).longitud(req.getLongitud())
            .build());
    }

    @Transactional
    public CentroAcopio actualizar(Long id, CentroAcopioRequest req) {
        CentroAcopio c = obtenerPorId(id);
        c.setNombre(req.getNombre()); c.setDireccion(req.getDireccion()); c.setRegion(req.getRegion());
        c.setCiudad(req.getCiudad()); c.setHorario(req.getHorario()); c.setTelefono(req.getTelefono());
        c.setQueRecibe(req.getQueRecibe()); c.setCapacidadActual(req.getCapacidadActual());
        c.setCapacidadMax(req.getCapacidadMax()); c.setLatitud(req.getLatitud()); c.setLongitud(req.getLongitud());
        return repo.save(c);
    }

    @Transactional
    public void eliminar(Long id) { repo.deleteById(id); }
}
