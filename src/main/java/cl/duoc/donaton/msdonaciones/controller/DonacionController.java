package cl.duoc.donaton.msdonaciones.controller;

import cl.duoc.donaton.msdonaciones.dto.DonacionRequest;
import cl.duoc.donaton.msdonaciones.dto.TopDonadorResponse;
import cl.duoc.donaton.msdonaciones.dto.TransparenciaResponse;
import cl.duoc.donaton.msdonaciones.model.Donacion;
import cl.duoc.donaton.msdonaciones.service.DonacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/donaciones")
@RequiredArgsConstructor
@Tag(name = "Donaciones", description = "Registro y consulta de donaciones")
public class DonacionController {

    private final DonacionService donacionService;

    @PostMapping
    @Operation(summary = "Crear donación")
    public ResponseEntity<Donacion> crear(@Valid @RequestBody DonacionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String donadorId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(donacionService.crear(request, donadorId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las donaciones (admin)")
    public ResponseEntity<List<Donacion>> listar() {
        return ResponseEntity.ok(donacionService.listarTodas());
    }

    @GetMapping("/mis-donaciones")
    @Operation(summary = "Mis donaciones (usuario autenticado)")
    public ResponseEntity<List<Donacion>> misDonaciones(
            @RequestHeader(value = "X-User-Id", required = false) String donadorId) {
        return ResponseEntity.ok(donacionService.listarPorDonador(donadorId));
    }

    @GetMapping("/top-donadores")
    @Operation(summary = "Top 10 donantes por monto total (público)")
    public ResponseEntity<List<TopDonadorResponse>> topDonadores() {
        return ResponseEntity.ok(donacionService.topDonadores());
    }

    @GetMapping("/transparencia")
    @Operation(summary = "Lista pública auditada de donaciones (público)")
    public ResponseEntity<List<TransparenciaResponse>> transparencia() {
        return ResponseEntity.ok(donacionService.transparencia());
    }

    @GetMapping("/ultimas")
    @Operation(summary = "Últimas N donaciones (público)")
    public ResponseEntity<List<Donacion>> ultimas(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(donacionService.listarUltimas(limit));
    }

    @GetMapping("/top")
    @Operation(summary = "Top donadores por monto (alias público para BFF)")
    public ResponseEntity<List<TopDonadorResponse>> top(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(donacionService.topDonadores());
    }

    @GetMapping("/total")
    @Operation(summary = "Suma total de todos los montos donados (público)")
    public ResponseEntity<BigDecimal> total() {
        return ResponseEntity.ok(donacionService.totalRecaudado());
    }

    @GetMapping("/count")
    @Operation(summary = "Cantidad total de donaciones (público)")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(donacionService.conteo());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar donación por ID (admin)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        donacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
