package com.app.cafeteria.services.mesa;

import java.util.List;

import com.app.cafeteria.dtos.mesa.TipoMesaResponse;

public interface TipoMesaService {
    List<TipoMesaResponse> listarTipos();
}

