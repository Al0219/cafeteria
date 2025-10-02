package com.app.cafeteria.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 30) String usuario,
        @NotBlank @Size(max = 15) String dpi,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 20) String telefono,
        @NotBlank @Size(max = 250) String direccion,
        @NotBlank String rolCodigo,
        @Size(min = 8, max = 120) String contrasenia,
        Boolean activo
) {}
