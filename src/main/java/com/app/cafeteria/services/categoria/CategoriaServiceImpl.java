package com.app.cafeteria.services.categoria;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.cafeteria.dtos.producto.CategoriaResponse;
import com.app.cafeteria.entities.Categoria;
import com.app.cafeteria.repositories.CategoriaRepository;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<CategoriaResponse> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getActivo());
    }
}

