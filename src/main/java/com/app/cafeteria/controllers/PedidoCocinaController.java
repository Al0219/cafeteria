package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.pedido.PedidoCocinaUpdateRequest;
import com.app.cafeteria.dtos.pedido.PedidoResponse;
import com.app.cafeteria.entities.enums.EstadoPedido;
import com.app.cafeteria.services.pedido.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos/cocina")
@Validated
public class PedidoCocinaController {

    private final PedidoService pedidoService;

    public PedidoCocinaController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(name = "estado", required = false) EstadoPedido estado) {
        return pedidoService.listarPedidosPorEstado(estado);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PedidoResponse avanzar(@PathVariable Integer id,
                                   @Valid @RequestBody PedidoCocinaUpdateRequest request) {
        return pedidoService.cambiarEstadoCocina(id, request.nuevoEstado(), request.usuarioId());
    }
}
