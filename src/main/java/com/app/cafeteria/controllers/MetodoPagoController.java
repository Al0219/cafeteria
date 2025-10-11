package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.venta.MetodoPagoResponse;
import com.app.cafeteria.services.venta.MetodoPagoService;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoService metodoPagoService;

    public MetodoPagoController(MetodoPagoService metodoPagoService) {
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping
    public List<MetodoPagoResponse> listar() {
        return metodoPagoService.listar();
    }
}

