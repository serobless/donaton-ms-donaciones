package cl.duoc.donaton.msdonaciones.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CausaRequest {
    @NotBlank @Size(max = 150) private String titulo;
    @Size(max = 1000) private String descripcion;
    @NotNull @DecimalMin("0.01") private BigDecimal meta;
    @Size(max = 80) private String categoria;
    @Size(max = 500) private String imagenUrl;
    private Integer diasRestantes;
    private Boolean destacada;
    @Size(max = 100) private String urgencia;
    private LocalDate fechaInicio;
}
