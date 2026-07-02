package cl.duoc.donaton.msdonaciones.controller;

import cl.duoc.donaton.msdonaciones.dto.TestimonioRequest;
import cl.duoc.donaton.msdonaciones.model.Testimonio;
import cl.duoc.donaton.msdonaciones.service.TestimonioService;
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
@RequestMapping("/api/testimonios")
@RequiredArgsConstructor
@Tag(name = "Testimonios", description = "Gestión de testimonios")
public class TestimonioController {

    private final TestimonioService testimonioService;

    @GetMapping
    @Operation(summary = "Listar testimonios aprobados (público)")
    public List<Testimonio> listar() {
        return testimonioService.listar();
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar testimonios pendientes de aprobación (ADMIN)")
    public List<Testimonio> pendientes() {
        return testimonioService.listarPendientes();
    }

    @PutMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar testimonio (ADMIN)")
    public Testimonio aprobar(@PathVariable Long id) {
        return testimonioService.aprobar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear testimonio (autenticado)")
    public Testimonio crear(
            @Valid @RequestBody TestimonioRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles,
            @RequestHeader(value = "X-User-Name", required = false) String userName) {
        return testimonioService.crear(req, userId, userName);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar testimonio (solo ADMIN)")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles) {
        if (userRoles == null || !userRoles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        testimonioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
