package com.app.cafeteria.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import com.app.cafeteria.entities.enums.EstadoPedido;

@Entity
@Table(name = "pedido_estado_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PedidoEstadoLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pedido_estado_log_seq")
    @SequenceGenerator(name = "pedido_estado_log_seq", sequenceName = "pedido_estado_log_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.RECIBIDO;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cambiado_por", nullable = false)
    private Usuario cambiadoPor;

    @Column(name = "cambiado_en", nullable = false)
    private OffsetDateTime cambiadoEn;
}
