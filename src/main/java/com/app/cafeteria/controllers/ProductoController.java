package com.app.cafeteria.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.app.cafeteria.dtos.producto.ProductoCreateRequest;
import com.app.cafeteria.dtos.producto.ProductoResponse;
import com.app.cafeteria.dtos.producto.ProductoUpdateRequest;
import com.app.cafeteria.services.producto.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
@Validated
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoCreateRequest request) {
        return productoService.crearProducto(request);
    }

    @GetMapping
    public List<ProductoResponse> listar() {
        return productoService.listarProductos();
    }

    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Integer id,
                                        @Valid @RequestBody ProductoUpdateRequest request) {
        return productoService.editarProducto(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
    }
}
