package com.api.usuarios.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.api.usuarios.config.security.SecuredEndpoint;
import com.api.usuarios.entities.roles.Roles;
import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.services.BlacklistService;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.services.UsuariosService;
import com.api.usuarios.utilities.JwtUtil;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuariosService usuarioService;
    private final RolesService rolesService;
    private final BlacklistService tokenBlacklist;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            if (!requiresAuthentication(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = getTokenFromRequest(request);
            if (token == null) {
                sendError(response, "Acceso denegado: Token no presente");
                return;
            }

            if (tokenBlacklist.existeTokenEnListaNegra(token)) {
                sendError(response, "Acceso denegado: Token revocado");
                return;
            }

            String userId;
            try {
                userId = jwtUtil.getUsernameFromToken(token);
            } catch (JwtException e) {
                sendError(response, "Acceso no autorizado: " + e.getMessage());
                return;
            }

            if (userId == null) {
                sendError(response, "Token inválido");
                return;
            }

            Usuarios usuario = usuarioService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Validaciones básicas
            if (!usuario.getEstatus() || !jwtUtil.isTokenValid(token, usuario)) {
                sendError(response, "Usuario inactivo o token inválido");
                return;
            }

            // --- Lookup manual de relaciones ---
            Roles rol = rolesService.findById(usuario.getId_rol()).orElse(null);
            usuario.setRol(rol);

            // --- Establecer autenticación ---
            setAuthentication(request, usuario);

            // Continuar la cadena
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // Cualquier excepción no manejada termina con 401
            sendError(response, "Acceso no autorizado: " + ex.getMessage());
        }
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = handlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() instanceof HandlerMethod method) {
                return method.hasMethodAnnotation(SecuredEndpoint.class)
                        || method.getBeanType().isAnnotationPresent(SecuredEndpoint.class);
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private void setAuthentication(HttpServletRequest request, Usuarios usuario) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if (usuario.getRol() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getDescripcion()));
        }

        var authToken = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
