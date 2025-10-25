package com.app.cafeteria.dtos.venta;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record VentaCreateRequest(
        @NotNull Integer pedidoId,
        @NotNull Integer cajeroId,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal descuento,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) @Digits(integer = 10, fraction = 2) BigDecimal propina,
        @Valid List<VentaDetalleRequest> detalles,
        @NotNull @Valid PagoRequest pago
) {}
