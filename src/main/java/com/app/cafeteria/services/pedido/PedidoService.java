package com.app.cafeteria.services.pedido;

import java.util.List;

import com.app.cafeteria.dtos.pedido.PedidoCerrarRequest;
import com.app.cafeteria.dtos.pedido.PedidoCreateRequest;
import com.app.cafeteria.dtos.pedido.PedidoResponse;
import com.app.cafeteria.dtos.pedido.PedidoUpdateRequest;

public interface PedidoService {

    PedidoResponse crearPedido(PedidoCreateRequest request);

    List<PedidoResponse> listarPedidos();

    PedidoResponse actualizarPedido(Integer id, PedidoUpdateRequest request);

    PedidoResponse cerrarPedido(Integer id, PedidoCerrarRequest request);
}
