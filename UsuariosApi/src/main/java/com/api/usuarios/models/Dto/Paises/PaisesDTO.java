package com.api.usuarios.models.Dto.Paises;

import com.api.usuarios.models.Dto.GenericDTO;

import lombok.Data;

@Data
public class PaisesDTO extends GenericDTO {
    private String nombre;
    private String nombreCorto;
    private String codigoPais;
    private String codigoTelefono;
    private String foto;
    private Boolean estatus;
}
