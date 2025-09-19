package com.app.cafeteria.Repositories;

import com.app.cafeteria.Entities.PedidoEstadoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoEstadoLogRepository extends JpaRepository<PedidoEstadoLog, Long> {}