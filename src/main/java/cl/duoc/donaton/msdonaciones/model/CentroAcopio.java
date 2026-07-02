package cl.duoc.donaton.msdonaciones.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "centros_acopio")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CentroAcopio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank @Size(max = 150) @Column(nullable = false, length = 150) private String nombre;
    @Column(length = 200) private String direccion;
    @Column(length = 80) private String region;
    @Column(length = 80) private String ciudad;
    @Column(length = 100) private String horario;
    @Column(length = 20) private String telefono;

    @ElementCollection
    @CollectionTable(name = "centro_acopio_que_recibe", joinColumns = @JoinColumn(name = "centro_id"))
    @Column(name = "item", length = 100)
    @Builder.Default private List<String> queRecibe = new ArrayList<>();

    private Integer capacidadActual;
    private Integer capacidadMax;
    @Builder.Default @Column(nullable = false) private Boolean activo = true;
    private Double latitud;
    private Double longitud;
    @Column(length = 30) private String unidadCapacidad;
}
