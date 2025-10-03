package com.app.cafeteria.services.producto;

import java.util.List;

import com.app.cafeteria.dtos.producto.ProductoCreateRequest;
import com.app.cafeteria.dtos.producto.ProductoResponse;
import com.app.cafeteria.dtos.producto.ProductoUpdateRequest;

public interface ProductoService {

    ProductoResponse crearProducto(ProductoCreateRequest request);

    List<ProductoResponse> listarProductos();

    ProductoResponse editarProducto(Integer id, ProductoUpdateRequest request);

    void eliminarProducto(Integer id);
}
