package com.api.usuarios.models.Dto.Usuarios;

import java.time.LocalDateTime;

import com.api.usuarios.models.Dto.GenericDTO;
import com.api.usuarios.models.Dto.Roles.RolDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class UsuariosDTO extends GenericDTO {

    private String correo;
    private String username;
    private String id_rol;
    private Boolean estatus;
    private Boolean verificado;
    private LocalDateTime lastLogin;
    @JsonInclude(JsonInclude.Include.NON_NULL)

    private UsuarioDetalleDTO detalle;
    @JsonInclude(JsonInclude.Include.NON_NULL)

    private RolDTO rolDetalle;
}
