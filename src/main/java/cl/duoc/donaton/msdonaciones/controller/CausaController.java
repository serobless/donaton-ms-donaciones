package cl.duoc.donaton.msdonaciones.controller;

import cl.duoc.donaton.msdonaciones.dto.CausaRequest;
import cl.duoc.donaton.msdonaciones.model.Causa;
import cl.duoc.donaton.msdonaciones.service.CausaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/causas")
@RequiredArgsConstructor
@Tag(name = "Causas", description = "Gestión de causas benéficas")
public class CausaController {

    private final CausaService causaService;

    @GetMapping
    @Operation(summary = "Listar causas activas (público)")
    public ResponseEntity<List<Causa>> listar() {
        return ResponseEntity.ok(causaService.listarActivas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de causa (público)")
    public ResponseEntity<Causa> detalle(@PathVariable Long id) {
        return ResponseEntity.ok(causaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear causa (admin)")
    public ResponseEntity<Causa> crear(@Valid @RequestBody CausaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(causaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar causa (admin)")
    public ResponseEntity<Causa> actualizar(@PathVariable Long id, @Valid @RequestBody CausaRequest request) {
        return ResponseEntity.ok(causaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar causa (admin)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        causaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
