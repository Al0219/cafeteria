// src/main/java/com/app/cafeteria/Repositories/TipoMesaRepository.java
package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.TipoMesa;

@Repository
public interface TipoMesaRepository extends JpaRepository<TipoMesa, Long> {}
