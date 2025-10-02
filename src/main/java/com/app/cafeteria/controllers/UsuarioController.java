package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.user.UsuarioCreateRequest;
import com.app.cafeteria.dtos.user.UsuarioResponse;
import com.app.cafeteria.dtos.user.UsuarioUpdateRequest;
import com.app.cafeteria.services.user.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return usuarioService.crearUsuario(request);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listarUsuarios();
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Integer id,
                                       @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.editarUsuario(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
    }
}
