
package com.api.usuarios.models.Dto.Usuarios;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import com.api.usuarios.annotations.AddBaseUrl;
import com.api.usuarios.annotations.EdadMinima;
import com.api.usuarios.models.Dto.Generos.GenerosDTO;
import com.api.usuarios.models.Dto.Paises.PaisesDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.api.usuarios.annotations.ExistId;
import com.api.usuarios.services.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UsuarioDetalleDTO {
    private String nombres;
    private String apellidos;
    @AddBaseUrl
    private String foto;
    @Pattern(message = "Formato inválido: el número debe tener 10 dígitos numéricos (sin espacios ni símbolos).", regexp = "^\\d{10}$")
    @Length(message = "El Numero de telefono debe tener al menos 10 digitos", min = 10, max = 10)
    private String telefono;
    @ExistId(service = PaisesService.class, message = "El pais no existe")

    private String id_pais;
    @EdadMinima(minEdad = 18, message = "Tu fecha de cumpleaños debe ser mayor o igual a 18 años")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private LocalDate fechaNacimiento;
    @ExistId(service = GenerosService.class, message = "El genero no existe")
    private String id_genero;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)

    private PaisesDTO paisDetalle;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)

    private GenerosDTO generoDetalle;
}
