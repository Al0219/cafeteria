package com.app.cafeteria.dtos.user;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String usuario,
        String dpi,
        String email,
        String telefono,
        String direccion,
        String rolCodigo,
        String rolNombre,
        Boolean activo
) {}
