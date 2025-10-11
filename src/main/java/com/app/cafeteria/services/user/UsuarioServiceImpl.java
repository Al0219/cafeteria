package com.app.cafeteria.services.user;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.cafeteria.dtos.user.UsuarioCreateRequest;
import com.app.cafeteria.dtos.user.UsuarioResponse;
import com.app.cafeteria.dtos.user.UsuarioUpdateRequest;
import com.app.cafeteria.entities.Rol;
import com.app.cafeteria.entities.Usuario;
import com.app.cafeteria.repositories.RolRepository;
import com.app.cafeteria.repositories.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final Argon2PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = new Argon2PasswordEncoder(16, 32, 1, 65536, 3);
    }

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioCreateRequest request) {
        validarCamposUnicos(request.usuario(), request.email(), request.dpi(), null);
        Rol rol = obtenerRol(request.rolCodigo());

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre())
                .usuario(request.usuario())
                .dpi(request.dpi())
                .email(request.email())
                .telefono(request.telefono())
                .direccion(request.direccion())
                .rol(rol)
                .contraseniaHash(passwordEncoder.encode(request.contrasenia()))
                .activo(Boolean.TRUE)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return toResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse editarUsuario(Integer id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarCamposUnicos(request.usuario(), request.email(), request.dpi(), usuario);
        Rol rol = obtenerRol(request.rolCodigo());

        usuario.setNombre(request.nombre());
        usuario.setUsuario(request.usuario());
        usuario.setDpi(request.dpi());
        usuario.setEmail(request.email());
        usuario.setTelefono(request.telefono());
        usuario.setDireccion(request.direccion());
        usuario.setRol(rol);

        if (request.contrasenia() != null && !request.contrasenia().isBlank()) {
            usuario.setContraseniaHash(passwordEncoder.encode(request.contrasenia()));
        }

        if (Objects.nonNull(request.activo())) {
            usuario.setActivo(request.activo());
        }

        return toResponse(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        usuario.setActivo(false);
    }

    private void validarCamposUnicos(String usuarioNuevo, String emailNuevo, String dpiNuevo, Usuario actual) {
        String usuarioActual = actual != null ? actual.getUsuario() : null;
        if (usuarioNuevo != null) {
            boolean cambioUsuario = usuarioActual == null || !usuarioNuevo.equalsIgnoreCase(usuarioActual);
            if (cambioUsuario && usuarioRepository.existsByUsuario(usuarioNuevo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya esta registrado");
            }
        }

        String emailActual = actual != null ? actual.getEmail() : null;
        if (emailNuevo != null) {
            boolean cambioEmail = emailActual == null || !emailNuevo.equalsIgnoreCase(emailActual);
            if (cambioEmail && usuarioRepository.existsByEmail(emailNuevo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
            }
        }

        String dpiActual = actual != null ? actual.getDpi() : null;
        if (dpiNuevo != null) {
            boolean cambioDpi = dpiActual == null || !dpiNuevo.equals(dpiActual);
            if (cambioDpi && usuarioRepository.existsByDpi(dpiNuevo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El DPI ya esta registrado");
            }
        }
    }

    private Rol obtenerRol(String rolCodigo) {
        return rolRepository.findByCodigo(rolCodigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no encontrado"));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getUsuario(),
                usuario.getDpi(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getRol() != null ? usuario.getRol().getCodigo() : null,
                usuario.getRol() != null ? usuario.getRol().getNombre() : null,
                usuario.getActivo()
        );
    }
}
