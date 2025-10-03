package com.app.cafeteria.dtos.mesa;

public record MesaResponse(
        Integer id,
        String nombre,
        Integer tipoId,
        String tipoNombre,
        Boolean activo
) {}
