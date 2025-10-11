package com.app.cafeteria.services.mesa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.cafeteria.dtos.mesa.TipoMesaResponse;
import com.app.cafeteria.entities.TipoMesa;
import com.app.cafeteria.repositories.TipoMesaRepository;

@Service
public class TipoMesaServiceImpl implements TipoMesaService {

    private final TipoMesaRepository tipoMesaRepository;

    public TipoMesaServiceImpl(TipoMesaRepository tipoMesaRepository) {
        this.tipoMesaRepository = tipoMesaRepository;
    }

    @Override
    public List<TipoMesaResponse> listarTipos() {
        return tipoMesaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private TipoMesaResponse toResponse(TipoMesa t) {
        return new TipoMesaResponse(t.getId(), t.getNombre());
    }
}

