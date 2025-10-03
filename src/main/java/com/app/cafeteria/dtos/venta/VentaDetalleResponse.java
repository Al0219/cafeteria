package com.app.cafeteria.dtos.venta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record VentaDetalleResponse(
        Integer id,
        Integer productoId,
        String nombreProducto,
        BigDecimal precioUnitario,
        Integer cantidad
) {}
