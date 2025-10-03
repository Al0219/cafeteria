package com.app.cafeteria.services.mesa;

import java.util.List;

import com.app.cafeteria.dtos.mesa.MesaCreateRequest;
import com.app.cafeteria.dtos.mesa.MesaResponse;
import com.app.cafeteria.dtos.mesa.MesaUpdateRequest;

public interface MesaService {

    MesaResponse crearMesa(MesaCreateRequest request);

    List<MesaResponse> listarMesas();

    MesaResponse editarMesa(Integer id, MesaUpdateRequest request);

    MesaResponse cambiarEstado(Integer id, boolean activo);

    void eliminarMesa(Integer id);
}
