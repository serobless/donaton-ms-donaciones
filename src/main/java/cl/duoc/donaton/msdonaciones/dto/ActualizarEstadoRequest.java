package cl.duoc.donaton.msdonaciones.dto;

import cl.duoc.donaton.msdonaciones.model.EstadoDonacion;
import lombok.Data;

@Data
public class ActualizarEstadoRequest {
    private EstadoDonacion estado;
}
