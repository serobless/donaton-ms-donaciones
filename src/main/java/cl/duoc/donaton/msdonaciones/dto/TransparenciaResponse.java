package cl.duoc.donaton.msdonaciones.dto;

import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransparenciaResponse {
    private Long id;
    private String donadorNombre;
    private BigDecimal monto;
    private TipoDonacion tipoDonacion;
    private LocalDateTime fecha;
    private String causaNombre;
}
