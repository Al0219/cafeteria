package com.app.cafeteria.services.producto;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.cafeteria.dtos.producto.ProductoCreateRequest;
import com.app.cafeteria.dtos.producto.ProductoResponse;
import com.app.cafeteria.dtos.producto.ProductoUpdateRequest;
import com.app.cafeteria.entities.Categoria;
import com.app.cafeteria.entities.Producto;
import com.app.cafeteria.repositories.CategoriaRepository;
import com.app.cafeteria.repositories.ProductoRepository;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public ProductoResponse crearProducto(ProductoCreateRequest request) {
        Categoria categoria = obtenerCategoria(request.categoriaId());
        validarNombreUnico(categoria.getId(), request.nombre(), null, null, null);

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);

        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .categoria(categoria)
                .precio(request.precio())
                .stock(request.stock())
                .imagenUrl(request.imagenUrl())
                .descripcion(request.descripcion())
                .activo(Boolean.TRUE)
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .build();

        Producto guardado = productoRepository.save(producto);
        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductoResponse editarProducto(Integer id, ProductoUpdateRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        Categoria categoria = obtenerCategoria(request.categoriaId());
        Integer categoriaActualId = producto.getCategoria() != null ? producto.getCategoria().getId() : null;
        String nombreActual = producto.getNombre();
        validarNombreUnico(categoria.getId(), request.nombre(), producto.getId(), categoriaActualId, nombreActual);

        producto.setNombre(request.nombre());
        producto.setCategoria(categoria);
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        producto.setImagenUrl(request.imagenUrl());
        producto.setDescripcion(request.descripcion());

        if (request.activo() != null) {
            producto.setActivo(request.activo());
        }

        producto.setActualizadoEn(OffsetDateTime.now(ZoneOffset.UTC));

        Producto actualizado = productoRepository.save(producto);
        return toResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.setActivo(false);
        producto.setActualizadoEn(OffsetDateTime.now(ZoneOffset.UTC));
        productoRepository.save(producto);
    }

    private void validarNombreUnico(Integer categoriaId, String nombre, Integer productoId, Integer categoriaActualId, String nombreActual) {
        boolean existe = productoRepository.existsByCategoriaIdAndNombreIgnoreCase(categoriaId, nombre);
        if (!existe) {
            return;
        }

        boolean mismoRegistro = productoId != null
                && categoriaActualId != null
                && categoriaActualId.equals(categoriaId)
                && nombreActual != null
                && nombreActual.equalsIgnoreCase(nombre);

        if (!mismoRegistro) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese nombre en la categoria");
        }
    }

    private Categoria obtenerCategoria(Integer categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria no encontrada"));
        if (Boolean.FALSE.equals(categoria.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoria esta inactiva");
        }
        return categoria;
    }

    private ProductoResponse toResponse(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.getPrecio(),
                producto.getStock(),
                producto.getImagenUrl(),
                producto.getDescripcion(),
                producto.getActivo(),
                producto.getCreadoEn(),
                producto.getActualizadoEn()
        );
    }
}
