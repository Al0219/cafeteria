package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "producto",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_producto_categoria_nombre", columnNames = {"categoria_id", "nombre"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "producto_seq")
    @SequenceGenerator(name = "producto_seq", sequenceName = "producto_id_seq", allocationSize = 1)
    private Integer id;

    @Column(length = 150, nullable = false)
    private String nombre;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @Column(name = "imagen_url", length = 400)
    private String imagenUrl;

    @Column(columnDefinition = "text")
    private String descripcion;
}
