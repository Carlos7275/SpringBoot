package com.api.usuarios.entities.usuarios;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.api.usuarios.entities.GenericEntity;
import com.api.usuarios.entities.roles.Roles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder

@Document(collection = "usuarios")
public class Usuarios extends GenericEntity {
    @Indexed(unique = true)
    private String correo;
    @Indexed(unique = true)
    private String username;
    private String password;

    private String id_rol;
    private Boolean estatus;
    private Boolean verificado;
    private LocalDateTime lastLogin;

    @DBRef(lazy = false)

    private Roles rol;
    private UsuarioDetalle detalle;
}
