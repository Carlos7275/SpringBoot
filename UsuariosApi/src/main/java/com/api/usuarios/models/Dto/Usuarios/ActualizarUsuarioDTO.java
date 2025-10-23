package com.api.usuarios.models.Dto.Usuarios;


import com.api.usuarios.annotations.ExistId;
import com.api.usuarios.services.RolesService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para actualizar usuario existente.
 */
@Data
public class ActualizarUsuarioDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 30, message = "El nombre de usuario debe tener entre 3 y 30 caracteres")

    private String username;

    @NotBlank(message = "El correo es obligatorio")
    
    @Email(message = "El correo no tiene un formato válido")
    private String correo;

    @ExistId(service = RolesService.class, message = "El rol no existe")
    private String id_rol;

    @Valid
    @NotNull(message = "Los datos del detalle del usuario son obligatorios")
    private UsuarioDetalleDTO detalle;

}

