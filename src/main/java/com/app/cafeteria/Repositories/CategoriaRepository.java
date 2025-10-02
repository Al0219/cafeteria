package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {}