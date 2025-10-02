package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_mesa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoMesa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tipo_mesa_seq")
    @SequenceGenerator(name = "tipo_mesa_seq", sequenceName = "tipo_mesa_id_seq", allocationSize = 1)
    private Integer id;

    @Column(length = 25, nullable = false, unique = true)
    private String nombre;
}
