package com.app.cafeteria.services.role;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.cafeteria.dtos.user.RolResponse;
import com.app.cafeteria.entities.Rol;
import com.app.cafeteria.repositories.RolRepository;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public List<RolResponse> listarRoles() {
        return rolRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RolResponse toResponse(Rol r) {
        return new RolResponse(r.getId(), r.getCodigo(), r.getNombre(), r.getDescripcion());
    }
}

