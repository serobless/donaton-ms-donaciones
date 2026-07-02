package cl.duoc.donaton.msdonaciones.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CentroAcopioRequest {
    @NotBlank @Size(max = 150) private String nombre;
    @Size(max = 200) private String direccion;
    @Size(max = 80) private String region;
    @Size(max = 80) private String ciudad;
    @Size(max = 100) private String horario;
    @Size(max = 20) private String telefono;
    private List<String> queRecibe = new ArrayList<>();
    private Integer capacidadActual;
    private Integer capacidadMax;
    private Double latitud;
    private Double longitud;
    private String unidadCapacidad;
}
