package com.app.cafeteria.services.user;

import java.util.List;

import com.app.cafeteria.dtos.user.UsuarioCreateRequest;
import com.app.cafeteria.dtos.user.UsuarioResponse;
import com.app.cafeteria.dtos.user.UsuarioUpdateRequest;

public interface UsuarioService {

    UsuarioResponse crearUsuario(UsuarioCreateRequest request);

    List<UsuarioResponse> listarUsuarios();

    UsuarioResponse obtenerUsuario(Integer id);

    UsuarioResponse editarUsuario(Integer id, UsuarioUpdateRequest request);

    void eliminarUsuario(Integer id);
}
