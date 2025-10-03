package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Mesa;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);
}
