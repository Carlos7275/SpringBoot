package com.api.usuarios.services;

import java.util.concurrent.CompletableFuture;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.models.Dto.Auth.LoginDto;
import com.api.usuarios.utilities.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UsuariosService _usuarioService;

    @Autowired
    private BCryptPasswordEncoder _passwordEncoder;

    @Autowired
    private BlacklistService _blacklistService;

    @AutoWired
    private JwtUtil _jwtUtil;

    @Async
    public CompletableFuture<String> iniciarSesion(LoginDto loginDto) throws BadRequestException {

        String correo = loginDto.email();

        Usuarios usuario = (Usuarios) _usuarioService.buscarPorCorreo(correo);
        // Verificar si la contraseña es correcta
        if (!_passwordEncoder.matches(loginDto.password(), usuario.getPassword())
                || !usuario.getEstatus()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verifique sus credenciales");
        }
        final Boolean sesionactiva = loginDto.sesionactiva() == null ? false : loginDto.sesionactiva();
        String jwt = _jwtUtil.generateToken(usuario, sesionactiva);

        return CompletableFuture.completedFuture(jwt);

    }

    @Async
    public CompletableFuture<Usuarios> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuarios currentUser = (Usuarios) authentication.getPrincipal();

        return CompletableFuture.completedFuture(currentUser);

    }

    protected void cerrarSesion(String token) {
        SecurityContextHolder.clearContext();
        _blacklistService.agregarListaNegraToken(token);

    }

}
