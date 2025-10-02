package com.app.cafeteria.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.cafeteria.entities.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {}