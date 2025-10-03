package com.app.cafeteria.dtos.pedido;

import java.util.List;

import com.app.cafeteria.entities.enums.EstadoPedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PedidoUpdateRequest(
        @Positive Integer mesaId,
        @Size(max = 50) String clienteNombre,
        @Size(max = 2000) String notas,
        EstadoPedido nuevoEstado,
        @Valid List<PedidoDetalleRequest> detalles,
        @NotNull Integer usuarioId
) {}
