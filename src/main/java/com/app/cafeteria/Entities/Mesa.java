package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mesa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mesa_seq")
    @SequenceGenerator(name = "mesa_seq", sequenceName = "mesa_id_seq", allocationSize = 1)
    private Integer id;

    @Column(length = 25, nullable = false, unique = true)
    private String nombre;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoMesa tipo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
