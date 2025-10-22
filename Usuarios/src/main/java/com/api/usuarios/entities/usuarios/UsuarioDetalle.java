package com.api.usuarios.entities.usuarios;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.mapping.Document;

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
    private String biografia;
    private String foto;
    private String telefono;
    private Paises pais;
    private LocalDate fechaNacimiento;
    private Generos genero;
}
