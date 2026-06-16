package cl.duoc.donaton.msdonaciones.controller;

import cl.duoc.donaton.msdonaciones.dto.TestimonioRequest;
import cl.duoc.donaton.msdonaciones.model.Testimonio;
import cl.duoc.donaton.msdonaciones.service.TestimonioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testimonios")
@RequiredArgsConstructor
@Tag(name = "Testimonios", description = "Gestión de testimonios")
public class TestimonioController {

    private final TestimonioService testimonioService;

    @GetMapping
    @Operation(summary = "Listar todos los testimonios (público)")
    public List<Testimonio> listar() {
        return testimonioService.listar();
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
}
