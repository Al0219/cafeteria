package com.app.cafeteria.services.venta;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.cafeteria.dtos.venta.MetodoPagoResponse;
import com.app.cafeteria.entities.MetodoPago;
import com.app.cafeteria.repositories.MetodoPagoRepository;

@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    private final MetodoPagoRepository metodoPagoRepository;

    public MetodoPagoServiceImpl(MetodoPagoRepository metodoPagoRepository) {
        this.metodoPagoRepository = metodoPagoRepository;
    }

    @Override
    public List<MetodoPagoResponse> listar() {
        return metodoPagoRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private MetodoPagoResponse toResponse(MetodoPago m) {
        return new MetodoPagoResponse(m.getId(), m.getNombre());
    }
}

