package com.api.usuarios.entities.usuarios;

import lombok.*;
import lombok.experimental.SuperBuilder;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

import com.api.usuarios.entities.GenericEntity;
import com.api.usuarios.entities.roles.Roles;
@EqualsAndHashCode(callSuper = true)

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@Document(collection = "usuarios")
public class Usuarios extends GenericEntity {
    private String correo;
    private String username;
    private String password;
    private Roles roles;
    private Boolean estatus;
    private Boolean verificado;
    private LocalDateTime lastLogin;

    private UsuarioDetalle detalle;
}
