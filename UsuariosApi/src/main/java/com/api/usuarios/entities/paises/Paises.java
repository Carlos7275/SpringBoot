package com.api.usuarios.entities.paises;

import lombok.*;

import org.springframework.data.mongodb.core.mapping.Document;

import com.api.usuarios.entities.GenericEntity;

import lombok.experimental.SuperBuilder;
@EqualsAndHashCode(callSuper = true)

@Document(collection = "paises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Paises extends GenericEntity {
    private String nombre;
    private String nombreCorto;
    private String codigoPais;
    private String codigoTelefono;
    private String foto;
    private Boolean estatus;
}
