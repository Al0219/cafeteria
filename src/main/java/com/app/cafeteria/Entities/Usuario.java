package com.app.cafeteria.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_id_seq", allocationSize = 1)
    private Long id;

    @Column(length = 15, nullable = false, unique = true)
    private String dpi;

    @Column(length = 50, nullable = false)
    private String nombre;

    @Column(length = 30, nullable = false, unique = true)
    private String usuario;

    @Column(name = "contrasenia_hash", length = 125, nullable = false)
    private String contraseniaHash;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(length = 20, nullable = false)
    private String telefono;

    @Column(length = 250, nullable = false)
    private String direccion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
