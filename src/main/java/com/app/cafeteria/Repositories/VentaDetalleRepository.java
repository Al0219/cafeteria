package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.VentaDetalle;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {}