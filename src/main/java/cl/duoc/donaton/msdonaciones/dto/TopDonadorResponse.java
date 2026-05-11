package cl.duoc.donaton.msdonaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopDonadorResponse {
    private String alias;
    private BigDecimal totalMonto;
}
