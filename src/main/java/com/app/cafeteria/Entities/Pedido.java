package com.app.cafeteria.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import com.app.cafeteria.Entities.enums.EstadoPedido;

@Entity
@Table(name = "pedido")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pedido_seq")
    @SequenceGenerator(name = "pedido_seq", sequenceName = "pedido_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id")
    private TipoServicio tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa; // puede ser null

    @Column(name = "cliente_nombre", length = 50, nullable = false)
    @Builder.Default
    private String clienteNombre = "CF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.RECIBIDO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean vendido = false;

    @Column(columnDefinition = "text")
    private String notas;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;
}
