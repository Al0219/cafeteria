package com.app.cafeteria.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rol_seq")
    @SequenceGenerator(name = "rol_seq", sequenceName = "rol_id_seq", allocationSize = 1)
    private Long id;

    @Column(length = 15, nullable = false, unique = true)
    private String codigo;

    @Column(length = 40, nullable = false, unique = true)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;
}
