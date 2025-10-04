package com.app.cafeteria.dtos.venta;

import java.math.BigDecimal;

public record PagoResponse(
        Integer id,
        Integer metodoPagoId,
        String metodoPagoNombre,
        BigDecimal monto
) {}
