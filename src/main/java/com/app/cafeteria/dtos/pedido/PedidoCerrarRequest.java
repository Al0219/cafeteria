package com.app.cafeteria.dtos.pedido;

import jakarta.validation.constraints.NotNull;

public record PedidoCerrarRequest(
        @NotNull Integer usuarioId
) {}
