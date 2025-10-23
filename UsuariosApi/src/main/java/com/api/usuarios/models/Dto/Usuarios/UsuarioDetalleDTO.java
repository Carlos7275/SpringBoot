
package com.api.usuarios.models.Dto.Usuarios;

import java.time.LocalDate;

import com.api.usuarios.annotations.AddBaseUrl;
import com.api.usuarios.models.Dto.Generos.GenerosDTO;
import com.api.usuarios.models.Dto.Paises.PaisesDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.api.usuarios.annotations.ExistId;
import com.api.usuarios.services.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UsuarioDetalleDTO {
    private String nombres;
    private String apellidos;
    @AddBaseUrl
    private String foto;
    private String telefono;
    @ExistId(service = PaisesService.class, message = "El pais no existe")

    private String id_pais;
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
