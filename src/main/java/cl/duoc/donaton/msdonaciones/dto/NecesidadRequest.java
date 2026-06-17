package cl.duoc.donaton.msdonaciones.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NecesidadRequest {
    @NotBlank @Size(max = 80) private String tipo;
    @Size(max = 200) private String descripcion;
    private Integer metaUnidades;
    private Integer unidadesActuales;
    private Boolean urgente;
    private Integer diasRestantes;
}
