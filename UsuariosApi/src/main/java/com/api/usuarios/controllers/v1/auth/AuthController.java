package com.api.usuarios.controllers.v1.auth;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutionException;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.usuarios.config.security.SecuredEndpoint;
import com.api.usuarios.models.Dto.Auth.LoginDto;
import com.api.usuarios.models.Dto.Usuarios.UsuariosDTO;
import com.api.usuarios.services.AuthService;
import com.api.usuarios.utilities.ResponseUtil;
import com.thewaterfall.throttler.configuration.annotation.Throttle;
import com.thewaterfall.throttler.processor.key.ThrottlerKeyType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Controlador para la autenticación de usuarios.
 */
@RestController
@Tag(name = "Auth", description = "Endpoints de autenticación de usuarios")
@RequestMapping("/api/v1/auth/")
public class AuthController {

        @Autowired
        private AuthService _authService;

        /**
         * Inicia sesión con las credenciales del usuario.
         *
         * @param loginDto DTO con correo/username y password
         * @return JWT token en caso de éxito
         */
        @Operation(summary = "Iniciar sesión", description = "Genera un token JWT para un usuario válido")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve el JWT"),
                        @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
        })
        @PostMapping("login")
        @Throttle(capacity = 3, refill = 3, period = 1, unit = ChronoUnit.MINUTES, key = ThrottlerKeyType.IP_ADDRESS)

        public ResponseEntity<?> login(
                        @Parameter(description = "Credenciales de usuario", required = true) @RequestBody @Valid LoginDto loginDto)
                        throws BadRequestException, InterruptedException, ExecutionException {

                String jwt = _authService.iniciarSesion(loginDto).get();

                return new ResponseEntity<>(
                                ResponseUtil.Response("Operación Exitosa", jwt),
                                HttpStatus.OK);
        }

        /**
         * Devuelve la información del usuario autenticado.
         *
         * @return Datos del usuario autenticado
         */
        @Operation(summary = "Usuario actual", description = "Obtiene los datos del usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente"),
                        @ApiResponse(responseCode = "401", description = "Usuario no autorizado")
        })
        @GetMapping("me")
        @SecuredEndpoint
        public ResponseEntity<?> me() throws InterruptedException, ExecutionException {
                UsuariosDTO usuario = _authService.me().get();
                return ResponseEntity.ok(ResponseUtil.Response("Operación Exitosa", usuario));
        }

        /**
         * Refresca el token JWT del usuario autenticado.
         *
         * @param request HttpServletRequest con el header Authorization
         * @return Nuevo token JWT
         */
        @Operation(summary = "Refrescar token", description = "Genera un nuevo token JWT válido", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refrescado correctamente"),
                        @ApiResponse(responseCode = "401", description = "Usuario no autorizado")
        })
        @SecuredEndpoint
        @GetMapping("refresh")
        public ResponseEntity<?> refreshToken(
                        @Parameter(hidden = true) HttpServletRequest request)
                        throws InterruptedException, ExecutionException {

                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                String token = authHeader.replace("Bearer ", "");
                String jwt = _authService.refrescarToken(token).get();

                return ResponseEntity.ok(ResponseUtil.Response("Operación Exitosa", jwt));
        }

        /**
         * Cierra la sesión del usuario, invalida el token y lo agrega a la blacklist.
         *
         * @param request HttpServletRequest con el header Authorization
         * @return Mensaje de éxito
         */
        @Operation(summary = "Cerrar sesión", description = "Invalida el token JWT y cierra la sesión del usuario", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente"),
                        @ApiResponse(responseCode = "401", description = "Usuario no autorizado")
        })
        @SecuredEndpoint
        @PostMapping("logout")
        public ResponseEntity<?> logout(
                        @Parameter(hidden = true) HttpServletRequest request) {

                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                String token = authHeader.replace("Bearer ", "");
                request.getSession().invalidate();

                _authService.cerrarSesion(token);

                return ResponseEntity.ok(ResponseUtil.Response("Operación Exitosa", "¡Se cerró la sesión con éxito!"));
        }
}
