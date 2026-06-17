package cl.duoc.donaton.msdonaciones.dto;

import cl.duoc.donaton.msdonaciones.model.TipoDonacion;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DonacionRequest {

    @NotNull
    private Long causaId;

    private Long centroAcopioId;

    @NotNull
    private TipoDonacion tipoDonacion;

    @DecimalMin("0.00")
    private BigDecimal monto;

    @Size(max = 100)
    private String donanteAlias;

    @Size(max = 500)
    private String descripcion;

    private Integer cantidad;

    @Size(max = 20)
    private String unidad;
}
