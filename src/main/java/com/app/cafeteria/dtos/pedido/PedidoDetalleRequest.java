package com.app.cafeteria.dtos.pedido;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PedidoDetalleRequest(
        @NotNull Integer productoId,
        @NotNull @Min(1) Integer cantidad,
        @Size(max = 1000) String observaciones
) {}
