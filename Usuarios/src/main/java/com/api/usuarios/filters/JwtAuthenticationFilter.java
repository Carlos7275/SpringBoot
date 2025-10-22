package com.api.usuarios.filters;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.services.BlacklistService;
import com.api.usuarios.services.UsuariosService;
import com.api.usuarios.utilities.JwtUtil;
import com.api.usuarios.config.security.SecuredEndpoint;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuariosService usuarioService;
    private final BlacklistService tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // ⚡ Obtener handler del endpoint
        Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);

        boolean requiresAuth = false;
        if (handler instanceof HandlerMethod method) {
            // ⚡ Solo si tiene la anotación SecuredEndpoint
            requiresAuth = method.hasMethodAnnotation(SecuredEndpoint.class) ||
                           method.getBeanType().isAnnotationPresent(SecuredEndpoint.class);
        }

        // ⚡ Si no requiere autenticación, todo público
        if (!requiresAuth) {
            filterChain.doFilter(request, response);
            return;
        }

        // ⚡ Endpoint protegido → validar JWT
        String token = getTokenFromRequest(request);
        if (token == null) {
            handleErrorToken(response, "Acceso no autorizado: Token no presente");
            return;
        }

        if (tokenBlacklist.existeTokenEnListaNegra(token)) {
            handleErrorToken(response, "Acceso no autorizado: Token revocado");
            return;
        }

        try {
            String userId = jwtUtil.getUsernameFromToken(token);
            if (userId == null) {
                handleErrorToken(response, "Acceso no autorizado: Token inválido");
                return;
            }

            usuarioService.findById(userId).ifPresentOrElse(usuario -> {
                try {
                    if (!usuario.getEstatus() || !jwtUtil.isTokenValid(token, usuario)) {
                        handleErrorToken(response, "Acceso no autorizado: Usuario inactivo o token inválido");
                        return;
                    }
                    setAuthentication(request, usuario);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, () -> {
                try {
                    handleErrorToken(response, "Acceso no autorizado: Usuario no encontrado");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (JwtException e) {
            handleErrorToken(response, "Acceso no autorizado: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    private void setAuthentication(HttpServletRequest request, Usuarios usuario) {
        var authToken = new UsernamePasswordAuthenticationToken(usuario, null, null);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void handleErrorToken(HttpServletResponse response, String error) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"" + error + "\"}");
    }
}
