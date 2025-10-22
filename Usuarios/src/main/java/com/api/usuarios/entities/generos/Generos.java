package com.api.usuarios.entities.generos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.api.usuarios.entities.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Document(collection = "generos")
@Data
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Builder
public class Generos extends GenericEntity {
    private String nombre;

}
