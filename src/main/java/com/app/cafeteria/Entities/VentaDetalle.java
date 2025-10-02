package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VentaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venta_detalle_seq")
    @SequenceGenerator(name = "venta_detalle_seq", sequenceName = "venta_detalle_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Column(name = "nombre_producto", length = 150, nullable = false)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Integer cantidad;
}
