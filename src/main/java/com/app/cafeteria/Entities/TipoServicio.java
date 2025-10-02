package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_servicio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipo_servicio_seq")
    @SequenceGenerator(name = "tipo_servicio_seq", sequenceName = "tipo_servicio_id_seq", allocationSize = 1)
    private Integer id;

    @Column(length = 25, nullable = false, unique = true)
    private String nombre;
}
