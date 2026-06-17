package cl.duoc.donaton.msdonaciones.controller;

import cl.duoc.donaton.msdonaciones.dto.NecesidadRequest;
import cl.duoc.donaton.msdonaciones.model.Necesidad;
import cl.duoc.donaton.msdonaciones.service.NecesidadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Necesidades")
public class NecesidadController {

    private final NecesidadService service;

    @GetMapping("/api/necesidades")
    public ResponseEntity<List<Necesidad>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/api/centros/{centroId}/necesidades")
    public ResponseEntity<List<Necesidad>> listarPorCentro(@PathVariable Long centroId) {
        return ResponseEntity.ok(service.listarPorCentro(centroId));
    }

    @PostMapping("/api/centros/{centroId}/necesidades")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Necesidad> crear(@PathVariable Long centroId,
                                            @Valid @RequestBody NecesidadRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(centroId, req));
    }

    @PutMapping("/api/necesidades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Necesidad> actualizar(@PathVariable Long id,
                                                 @Valid @RequestBody NecesidadRequest req) {
        return ResponseEntity.ok(service.actualizar(id, req));
    }

    @DeleteMapping("/api/necesidades/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
