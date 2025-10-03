package com.app.cafeteria.services.mesa;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.cafeteria.dtos.mesa.MesaCreateRequest;
import com.app.cafeteria.dtos.mesa.MesaResponse;
import com.app.cafeteria.dtos.mesa.MesaUpdateRequest;
import com.app.cafeteria.entities.Mesa;
import com.app.cafeteria.entities.TipoMesa;
import com.app.cafeteria.repositories.MesaRepository;
import com.app.cafeteria.repositories.TipoMesaRepository;

@Service
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final TipoMesaRepository tipoMesaRepository;

    public MesaServiceImpl(MesaRepository mesaRepository, TipoMesaRepository tipoMesaRepository) {
        this.mesaRepository = mesaRepository;
        this.tipoMesaRepository = tipoMesaRepository;
    }

    @Override
    @Transactional
    public MesaResponse crearMesa(MesaCreateRequest request) {
        validarNombreUnico(request.nombre(), null);
        TipoMesa tipoMesa = obtenerTipoMesa(request.tipoId());

        Mesa mesa = Mesa.builder()
                .nombre(request.nombre())
                .tipo(tipoMesa)
                .activo(Boolean.TRUE)
                .build();

        Mesa guardada = mesaRepository.save(mesa);
        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponse> listarMesas() {
        return mesaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MesaResponse editarMesa(Integer id, MesaUpdateRequest request) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));

        validarNombreUnico(request.nombre(), mesa.getId());
        TipoMesa tipoMesa = obtenerTipoMesa(request.tipoId());

        mesa.setNombre(request.nombre());
        mesa.setTipo(tipoMesa);

        if (request.activo() != null) {
            mesa.setActivo(request.activo());
        }

        return toResponse(mesa);
    }

    @Override
    @Transactional
    public MesaResponse cambiarEstado(Integer id, boolean activo) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
        mesa.setActivo(activo);
        return toResponse(mesa);
    }

    @Override
    @Transactional
    public void eliminarMesa(Integer id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
        mesa.setActivo(false);
    }

    private void validarNombreUnico(String nombre, Integer idActual) {
        boolean existe = mesaRepository.existsByNombreIgnoreCase(nombre);
        if (existe) {
            if (idActual == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la mesa ya existe");
            }
            Mesa mesaActual = mesaRepository.findById(idActual)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
            if (!mesaActual.getNombre().equalsIgnoreCase(nombre)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de la mesa ya existe");
            }
        }
    }

    private TipoMesa obtenerTipoMesa(Integer tipoId) {
        return tipoMesaRepository.findById(tipoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de mesa no encontrado"));
    }

    private MesaResponse toResponse(Mesa mesa) {
        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getTipo() != null ? mesa.getTipo().getId() : null,
                mesa.getTipo() != null ? mesa.getTipo().getNombre() : null,
                mesa.getActivo()
        );
    }
}
