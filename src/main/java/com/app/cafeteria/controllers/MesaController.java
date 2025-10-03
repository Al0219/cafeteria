package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.mesa.MesaCreateRequest;
import com.app.cafeteria.dtos.mesa.MesaResponse;
import com.app.cafeteria.dtos.mesa.MesaUpdateRequest;
import com.app.cafeteria.services.mesa.MesaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mesas")
@Validated
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MesaResponse crear(@Valid @RequestBody MesaCreateRequest request) {
        return mesaService.crearMesa(request);
    }

    @GetMapping
    public List<MesaResponse> listar() {
        return mesaService.listarMesas();
    }

    @PutMapping("/{id}")
    public MesaResponse actualizar(@PathVariable Integer id,
                                    @Valid @RequestBody MesaUpdateRequest request) {
        return mesaService.editarMesa(id, request);
    }

    @PatchMapping("/{id}/estado/{activo}")
    public MesaResponse cambiarEstado(@PathVariable Integer id, @PathVariable boolean activo) {
        return mesaService.cambiarEstado(id, activo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        mesaService.eliminarMesa(id);
    }
}
