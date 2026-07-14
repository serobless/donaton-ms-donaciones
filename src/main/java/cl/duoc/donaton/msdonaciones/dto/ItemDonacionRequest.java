package cl.duoc.donaton.msdonaciones.dto;

import lombok.Data;

@Data
public class ItemDonacionRequest {
    private String descripcion;
    private Integer cantidad;
    private String unidad;
}
