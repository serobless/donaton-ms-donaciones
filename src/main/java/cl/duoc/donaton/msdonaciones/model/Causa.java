package cl.duoc.donaton.msdonaciones.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "causas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Causa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nombre;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descripcion;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal meta;

    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal recaudado = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = true;

    @Size(max = 80)
    @Column(length = 80)
    private String categoria;

    @Size(max = 500)
    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "dias_restantes")
    private Integer diasRestantes;
}
