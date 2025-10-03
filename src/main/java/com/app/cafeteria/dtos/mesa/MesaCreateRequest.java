package com.app.cafeteria.dtos.mesa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MesaCreateRequest(
        @NotBlank @Size(max = 25) String nombre,
        @NotNull Integer tipoId
) {}
