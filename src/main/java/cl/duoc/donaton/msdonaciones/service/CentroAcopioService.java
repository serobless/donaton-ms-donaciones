package cl.duoc.donaton.msdonaciones.service;

import cl.duoc.donaton.msdonaciones.dto.CentroAcopioRequest;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.repository.CentroAcopioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
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
        CentroAcopio centro = CentroAcopio.builder()
            .nombre(req.getNombre()).direccion(req.getDireccion()).region(req.getRegion())
            .ciudad(req.getCiudad()).horario(req.getHorario()).telefono(req.getTelefono())
            .queRecibe(req.getQueRecibe()).capacidadActual(req.getCapacidadActual())
            .capacidadMax(req.getCapacidadMax()).latitud(req.getLatitud()).longitud(req.getLongitud())
            .unidadCapacidad(req.getUnidadCapacidad())
            .build();
        geocodificarDireccion(centro);
        return repo.save(centro);
    }

    @Transactional
    public CentroAcopio actualizar(Long id, CentroAcopioRequest req) {
        CentroAcopio c = obtenerPorId(id);
        c.setNombre(req.getNombre()); c.setDireccion(req.getDireccion()); c.setRegion(req.getRegion());
        c.setCiudad(req.getCiudad()); c.setHorario(req.getHorario()); c.setTelefono(req.getTelefono());
        c.setQueRecibe(req.getQueRecibe()); c.setCapacidadActual(req.getCapacidadActual());
        c.setCapacidadMax(req.getCapacidadMax()); c.setLatitud(req.getLatitud()); c.setLongitud(req.getLongitud());
        c.setUnidadCapacidad(req.getUnidadCapacidad());
        geocodificarDireccion(c);
        return repo.save(c);
    }

    @Transactional
    public void eliminar(Long id) { repo.deleteById(id); }

    private void geocodificarDireccion(CentroAcopio centro) {
        if (centro.getDireccion() == null || centro.getCiudad() == null) return;
        try {
            String query = URLEncoder.encode(
                centro.getDireccion() + ", " + centro.getCiudad() + ", Chile",
                StandardCharsets.UTF_8);
            String url = "https://nominatim.openstreetmap.org/search?q=" + query + "&format=json&limit=1";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url))
                .header("User-Agent", "Donaton/1.0").build();
            HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode arr = mapper.readTree(resp.body());
            if (arr.isArray() && arr.size() > 0) {
                centro.setLatitud(arr.get(0).get("lat").asDouble());
                centro.setLongitud(arr.get(0).get("lon").asDouble());
            }
        } catch (Exception e) {
            log.warn("No se pudo geocodificar: {}", e.getMessage());
        }
    }
}
