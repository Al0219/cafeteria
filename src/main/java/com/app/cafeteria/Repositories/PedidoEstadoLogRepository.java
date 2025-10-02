package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.PedidoEstadoLog;

@Repository
public interface PedidoEstadoLogRepository extends JpaRepository<PedidoEstadoLog, Integer> {}
