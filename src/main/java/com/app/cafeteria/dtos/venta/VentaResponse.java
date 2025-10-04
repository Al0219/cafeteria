package com.app.cafeteria.dtos.venta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record VentaResponse(
        Integer id,
        Integer pedidoId,
        Integer cajeroId,
        String cajeroNombre,
        OffsetDateTime fecha,
        BigDecimal subtotal,
        BigDecimal descuento,
        BigDecimal propina,
        BigDecimal impuesto,
        BigDecimal total,
        List<VentaDetalleResponse> detalles,
        PagoResponse pago
) {}
