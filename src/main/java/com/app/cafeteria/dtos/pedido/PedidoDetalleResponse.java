package com.app.cafeteria.dtos.pedido;

import java.math.BigDecimal;

public record PedidoDetalleResponse(
        Integer id,
        Integer productoId,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad,
        String observaciones
) {}
