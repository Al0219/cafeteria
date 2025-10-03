package com.app.cafeteria.services.auth;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.app.cafeteria.dtos.auth.LoginRequest;
import com.app.cafeteria.dtos.auth.LoginResponse;
import com.app.cafeteria.entities.Rol;
import com.app.cafeteria.entities.Usuario;
import com.app.cafeteria.repositories.UsuarioRepository;
import com.app.cafeteria.security.JwtTokenProvider;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String identifier = request.usernameOrEmail().trim();
        Usuario usuario = usuarioRepository.findByUsuario(identifier)
                .or(() -> usuarioRepository.findByEmail(identifier))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), usuario.getContraseniaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        Rol rol = usuario.getRol();
        List<String> roles = rol != null ? Collections.singletonList(rol.getCodigo()) : Collections.emptyList();
        String token = jwtTokenProvider.generateToken(usuario, roles);
        OffsetDateTime expiresAt = jwtTokenProvider.getExpirationAsOffsetDateTime(token);

        return new LoginResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getEmail(),
                usuario.getNombre(),
                roles,
                token,
                expiresAt
        );
    }
}
