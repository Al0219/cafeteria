package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {}