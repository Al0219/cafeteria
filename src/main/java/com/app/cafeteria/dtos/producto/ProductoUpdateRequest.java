package com.app.cafeteria.dtos.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoUpdateRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotNull Integer categoriaId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        @Size(max = 400) String imagenUrl,
        @Size(max = 2000) String descripcion,
        Boolean activo
) {}
