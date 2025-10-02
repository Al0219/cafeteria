package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pago")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pago_seq")
    @SequenceGenerator(name = "pago_seq", sequenceName = "pago_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo", nullable = false)
    private MetodoPago metodo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;
}
