// src/main/java/com/app/cafeteria/Repositories/TipoMesaRepository.java
package com.app.cafeteria.Repositories;

import com.app.cafeteria.Entities.TipoMesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMesaRepository extends JpaRepository<TipoMesa, Long> {}
