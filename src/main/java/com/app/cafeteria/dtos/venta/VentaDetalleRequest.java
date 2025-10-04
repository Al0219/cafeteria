package com.app.cafeteria.dtos.venta;

import java.math.BigDecimal;

public record VentaDetalleRequest(
        Integer productoId,
        Integer cantidad,
        BigDecimal precioUnitario
) {}
