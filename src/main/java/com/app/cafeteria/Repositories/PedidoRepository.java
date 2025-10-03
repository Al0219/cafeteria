package com.app.cafeteria.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Pedido;
import com.app.cafeteria.entities.enums.EstadoPedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    boolean existsByMesaIdAndVendidoFalseAndEstadoNot(Integer mesaId, EstadoPedido estado);

    List<Pedido> findByEstado(EstadoPedido estado);
}
