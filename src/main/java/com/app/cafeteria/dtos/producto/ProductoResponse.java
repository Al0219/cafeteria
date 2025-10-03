package com.app.cafeteria.dtos.producto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProductoResponse(
        Integer id,
        String nombre,
        Integer categoriaId,
        String categoriaNombre,
        BigDecimal precio,
        Integer stock,
        String imagenUrl,
        String descripcion,
        Boolean activo,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn
) {}
