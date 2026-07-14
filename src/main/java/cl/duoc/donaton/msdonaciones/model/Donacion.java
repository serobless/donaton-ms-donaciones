package cl.duoc.donaton.msdonaciones.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_donacion", nullable = false, length = 20)
    private TipoDonacion tipoDonacion;

    @Size(max = 100)
    @Column(name = "donante_alias", length = 100)
    private String donanteAlias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "causa_id", nullable = false)
    private Causa causa;

    @JsonIgnoreProperties({"queRecibe", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_acopio_id")
    private CentroAcopio centroAcopio;

    @Column(name = "donador_id")
    private String donadorId;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Size(max = 20)
    @Column(name = "unidad", length = 20)
    private String unidad;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDonacion estado = EstadoDonacion.PENDIENTE;

    @Column(length = 250)
    private String direccionDonante;

    @Builder.Default
    @Column(nullable = false)
    private Boolean esEmpresa = false;

    @Column(length = 150)
    private String nombreEmpresa;

    @Builder.Default
    @Column(name = "requiere_aprobacion", nullable = false)
    private Boolean requiereAprobacion = false;
}
