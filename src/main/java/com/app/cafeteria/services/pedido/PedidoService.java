package com.app.cafeteria.services.pedido;

import java.util.List;

import com.app.cafeteria.dtos.pedido.PedidoCerrarRequest;
import com.app.cafeteria.dtos.pedido.PedidoCreateRequest;
import com.app.cafeteria.dtos.pedido.PedidoResponse;
import com.app.cafeteria.dtos.pedido.PedidoUpdateRequest;
import com.app.cafeteria.entities.enums.EstadoPedido;

public interface PedidoService {

    PedidoResponse crearPedido(PedidoCreateRequest request);

    List<PedidoResponse> listarPedidos();

    List<PedidoResponse> listarPedidosPorEstado(EstadoPedido estado);

    PedidoResponse actualizarPedido(Integer id, PedidoUpdateRequest request);

    PedidoResponse cambiarEstadoCocina(Integer id, EstadoPedido nuevoEstado, Integer usuarioId);

    PedidoResponse cerrarPedido(Integer id, PedidoCerrarRequest request);
}
