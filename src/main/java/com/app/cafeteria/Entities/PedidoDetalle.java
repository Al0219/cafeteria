package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pedido_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pedido_detalle_seq")
    @SequenceGenerator(name = "pedido_detalle_seq", sequenceName = "pedido_detalle_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "nombre_producto", length = 150, nullable = false)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(columnDefinition = "text")
    private String observaciones;
}
