package com.api.usuarios.config.mapper;

import java.time.LocalDate;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.models.Dto.Usuarios.UsuariosDTO;

import jakarta.annotation.PostConstruct;

@Configuration
public class ModelMapperConfig {

    private final ModelMapper modelMapper = new ModelMapper();

    @Bean
    public ModelMapper modelMapper() {
        return modelMapper;
    }

    @PostConstruct
    public void configureModelMapper() {

        // ⚡ Crear un TypeMap vacío de Usuarios -> UsuariosDTO
        var typeMap = modelMapper.createTypeMap(Usuarios.class, UsuariosDTO.class);
        typeMap.addMappings(mapper -> {
            mapper.map(Usuarios::getCorreo, UsuariosDTO::setCorreo);
            mapper.map(Usuarios::getUsername, UsuariosDTO::setUsername);
            mapper.map(Usuarios::getId_rol, UsuariosDTO::setId_rol);
            mapper.map(Usuarios::getEstatus, UsuariosDTO::setEstatus);
            mapper.map(Usuarios::getVerificado, UsuariosDTO::setVerificado);
            mapper.map(Usuarios::getLastLogin, UsuariosDTO::setLastLogin);

            // ⚡ Mapeo manual de detalles
            mapper.map(src -> src.getDetalle().getNombres(), (dest, v) -> dest.getDetalle().setNombres((String) v));
            mapper.map(src -> src.getDetalle().getApellidos(), (dest, v) -> dest.getDetalle().setApellidos((String) v));
            mapper.map(src -> src.getDetalle().getFoto(), (dest, v) -> dest.getDetalle().setFoto((String) v));
            mapper.map(src -> src.getDetalle().getTelefono(), (dest, v) -> dest.getDetalle().setTelefono((String) v));
            mapper.map(src -> src.getDetalle().getFechaNacimiento(),
                    (dest, v) -> dest.getDetalle().setFechaNacimiento((LocalDate) v));
            mapper.map(src -> src.getDetalle().getId_genero(), (dest, v) -> dest.getDetalle().setId_genero((String) v));
            mapper.map(src -> src.getDetalle().getId_pais(), (dest, v) -> dest.getDetalle().setId_pais((String) v));
        });

    }
}
