package cl.duoc.donaton.msdonaciones.controller;

import cl.duoc.donaton.msdonaciones.dto.CentroAcopioRequest;
import cl.duoc.donaton.msdonaciones.model.CentroAcopio;
import cl.duoc.donaton.msdonaciones.service.CentroAcopioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/centros")
@RequiredArgsConstructor
@Tag(name = "Centros de Acopio")
public class CentroAcopioController {

    private final CentroAcopioService service;

    @GetMapping
    @Operation(summary = "Listar centros activos (público)")
    public ResponseEntity<List<CentroAcopio>> listar() { return ResponseEntity.ok(service.listar()); }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de centro (público)")
    public ResponseEntity<CentroAcopio> detalle(@PathVariable Long id) { return ResponseEntity.ok(service.obtenerPorId(id)); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear centro (admin)")
    public ResponseEntity<CentroAcopio> crear(@Valid @RequestBody CentroAcopioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar centro (admin)")
    public ResponseEntity<CentroAcopio> actualizar(@PathVariable Long id, @Valid @RequestBody CentroAcopioRequest req) {
        return ResponseEntity.ok(service.actualizar(id, req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CENTRO_ADMIN')")
    @Operation(summary = "Actualización parcial de capacidad (admin / encargado)")
    public ResponseEntity<CentroAcopio> actualizarParcial(@PathVariable Long id,
                                                           @RequestBody Map<String, Object> body) {
        Integer capacidadActual = body.containsKey("capacidadActual")
            ? ((Number) body.get("capacidadActual")).intValue() : null;
        String unidadCapacidad = body.containsKey("unidadCapacidad")
            ? (String) body.get("unidadCapacidad") : null;
        return ResponseEntity.ok(service.actualizarCapacidad(id, capacidadActual, unidadCapacidad));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar centro (admin)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
