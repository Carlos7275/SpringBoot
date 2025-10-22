package com.api.usuarios.entities.roles;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.api.usuarios.entities.GenericEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)

@Document(collection = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Roles extends GenericEntity {

    private String descripcion;
}
