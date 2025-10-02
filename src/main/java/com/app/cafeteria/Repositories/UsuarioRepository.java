package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {}