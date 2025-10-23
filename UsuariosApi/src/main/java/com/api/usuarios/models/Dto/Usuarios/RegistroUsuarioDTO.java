package com.api.usuarios.models.Dto.Usuarios;

import java.io.Serializable;

import com.api.usuarios.annotations.ExistId;
import com.api.usuarios.annotations.UniqueField;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.services.UsuariosService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO principal para registrar un nuevo usuario junto con su detalle.
 */
@Data
public class RegistroUsuarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")
    @UniqueField(service = UsuariosService.class, field = "username", message = "El username ya esta registrado en el sistema")

    private String username;

    @NotBlank(message = "El correo es obligatorio")
    @UniqueField(service = UsuariosService.class, message = "El correo ya esta registrado en el sistema", field = "correo")

    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @ExistId(service = RolesService.class, message = "El rol no existe")

    private String id_rol;

    /**
     * Datos de detalle del usuario (nombres, apellidos, etc.)
     */
    @Valid
    @NotNull(message = "Los datos del detalle del usuario son obligatorios")
    private UsuarioDetalleDTO detalle;

}
