package com.app.cafeteria.dtos.pedido;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PedidoCreateRequest(
        @NotNull Integer tipoServicioId,
        @Positive Integer mesaId,
        @NotBlank @Size(max = 50) String clienteNombre,
        @Size(max = 2000) String notas,
        @NotNull Integer usuarioId,
        @NotEmpty @Valid List<PedidoDetalleRequest> detalles
) {}
