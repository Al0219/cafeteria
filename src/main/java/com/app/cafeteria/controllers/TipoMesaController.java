package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.mesa.TipoMesaResponse;
import com.app.cafeteria.services.mesa.TipoMesaService;

@RestController
@RequestMapping("/api/mesas/tipos")
public class TipoMesaController {

    private final TipoMesaService tipoMesaService;

    public TipoMesaController(TipoMesaService tipoMesaService) {
        this.tipoMesaService = tipoMesaService;
    }

    @GetMapping
    public List<TipoMesaResponse> listar() {
        return tipoMesaService.listarTipos();
    }
}

