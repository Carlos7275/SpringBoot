package com.api.usuarios.entities.usuarios;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.api.usuarios.entities.generos.Generos;
import com.api.usuarios.entities.paises.Paises;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "usuario_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDetalle {

    private String nombres;
    private String apellidos;
    
    private String foto;
    private String telefono;
    private String id_pais;
    private LocalDate fechaNacimiento;
    private String id_genero;

    @DBRef(lazy = false)

    private Generos generoDetalle;
    @DBRef(lazy = false)

    private Paises paisDetalle;

}
