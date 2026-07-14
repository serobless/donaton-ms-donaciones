package cl.duoc.donaton.msdonaciones.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items_donacion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donacion_id", nullable = false)
    private Donacion donacion;

    @Column(nullable = false)
    private String descripcion;

    private Integer cantidad;

    @Column(length = 50)
    private String unidad;
}
