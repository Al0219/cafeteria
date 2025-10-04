package com.app.cafeteria.dtos.pedido;

import com.app.cafeteria.entities.enums.EstadoPedido;

import jakarta.validation.constraints.NotNull;

public record PedidoCocinaUpdateRequest(
        @NotNull EstadoPedido nuevoEstado,
        @NotNull Integer usuarioId
) {}
