package com.app.cafeteria.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.venta.VentaCreateRequest;
import com.app.cafeteria.dtos.venta.VentaResponse;
import com.app.cafeteria.services.venta.VentaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
@Validated
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public VentaResponse registrarVenta(@Valid @RequestBody VentaCreateRequest request) {
        return ventaService.registrarVenta(request);
    }

    @GetMapping("/{id}/ticket")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Integer id) {
        byte[] contenido = ventaService.generarTicket(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket-" + id + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(contenido);
    }
}
