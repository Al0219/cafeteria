package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {}