package com.app.cafeteria.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.TipoMesa;

@Repository
public interface TipoMesaRepository extends JpaRepository<TipoMesa, Integer> {

    Optional<TipoMesa> findByNombreIgnoreCase(String nombre);
}
