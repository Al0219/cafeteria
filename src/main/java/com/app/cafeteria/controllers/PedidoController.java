package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.pedido.PedidoCerrarRequest;
import com.app.cafeteria.dtos.pedido.PedidoCreateRequest;
import com.app.cafeteria.dtos.pedido.PedidoResponse;
import com.app.cafeteria.dtos.pedido.PedidoUpdateRequest;
import com.app.cafeteria.services.pedido.PedidoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
@Validated
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse crear(@Valid @RequestBody PedidoCreateRequest request,
                                @RequestParam(name = "confirm", defaultValue = "false") boolean confirm) {
        return pedidoService.crearPedido(request, confirm);
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidoService.listarPedidos();
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Integer id) {
        return pedidoService.obtenerPedido(id);
    }

    @PutMapping("/{id}")
    public PedidoResponse actualizar(@PathVariable Integer id,
                                      @Valid @RequestBody PedidoUpdateRequest request) {
        return pedidoService.actualizarPedido(id, request);
    }

    @PutMapping("/{id}/cerrar")
    public PedidoResponse cerrar(@PathVariable Integer id,
                                  @Valid @RequestBody PedidoCerrarRequest request) {
        return pedidoService.cerrarPedido(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        pedidoService.eliminarPedido(id);
    }
}
