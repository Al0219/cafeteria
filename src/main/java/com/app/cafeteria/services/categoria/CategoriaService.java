package com.app.cafeteria.services.categoria;

import java.util.List;
import com.app.cafeteria.dtos.producto.CategoriaResponse;

public interface CategoriaService {
    List<CategoriaResponse> listarCategorias();
}

