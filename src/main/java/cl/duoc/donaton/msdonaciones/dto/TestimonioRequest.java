package cl.duoc.donaton.msdonaciones.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestimonioRequest {

    @NotBlank
    private String titulo;

    @NotBlank
    private String contenido;

    private String imagenUrl;
}
