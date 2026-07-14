package cl.duoc.donaton.msdonaciones.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "necesidades")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Necesidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "centro_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "queRecibe",
            "latitud", "longitud", "horario", "telefono", "descripcion",
            "imagenUrl", "unidadCapacidad", "activo", "capacidadMax", "capacidadActual"})
    private CentroAcopio centro;

    @NotBlank @Size(max = 80) @Column(nullable = false, length = 80) private String tipo;
    @Column(length = 200) private String descripcion;
    private Integer metaUnidades;
    @Builder.Default private Integer unidadesActuales = 0;
    @Builder.Default @Column(nullable = false) private Boolean urgente = false;
    private Integer diasRestantes;
    @Builder.Default @Column(nullable = false) private Boolean activa = true;
}
