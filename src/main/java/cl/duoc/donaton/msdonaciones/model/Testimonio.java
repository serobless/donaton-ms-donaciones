package cl.duoc.donaton.msdonaciones.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "testimonios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Testimonio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String contenido;

    private String autorId;

    private String autorNombre;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String imagenUrl;
}
