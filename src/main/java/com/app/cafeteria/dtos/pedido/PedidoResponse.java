package com.app.cafeteria.dtos.pedido;

import java.time.OffsetDateTime;
import java.util.List;

import com.app.cafeteria.entities.enums.EstadoPedido;

public record PedidoResponse(
        Integer id,
        Integer tipoServicioId,
        String tipoServicioNombre,
        Integer mesaId,
        String mesaNombre,
        String clienteNombre,
        EstadoPedido estado,
        Boolean vendido,
        String notas,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn,
        Integer creadoPorId,
        String creadoPorNombre,
        List<PedidoDetalleResponse> detalles
) {}
