package com.api.usuarios.messaging;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.api.usuarios.entities.roles.Roles;
import com.api.usuarios.entities.usuarios.UsuarioDetalle;
import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.models.Dto.Usuarios.RegistroUsuarioDTO;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.services.UsuariosService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioConsumer {

    private final UsuariosService _usuarioService;
    private final RolesService _rolesService;
    private final ModelMapper _modelMapper;
    private final PasswordEncoder _passwordEncoder;

    private static final String QUEUE_USUARIOS = "usuarios.queue";

    @RabbitListener(queues = QUEUE_USUARIOS)
    public void procesarUsuarios(String message) {
        ObjectMapper mapper = new ObjectMapper();
        List<RegistroUsuarioDTO> listaDTO;

        try {
            listaDTO = mapper.readValue(message, new TypeReference<List<RegistroUsuarioDTO>>() {
            });
        } catch (Exception e) {
            System.err.println("Error parseando JSON: " + e.getMessage());
            return;
        }

        List<String> usuariosGuardados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (RegistroUsuarioDTO dto : listaDTO) {
            try {
                Usuarios usuario = _modelMapper.map(dto, Usuarios.class);
                UsuarioDetalle detalle = _modelMapper.map(dto.getDetalle(), UsuarioDetalle.class);

                // Foto default
                if (detalle.getFoto() == null || detalle.getFoto().isEmpty()) {
                    detalle.setFoto("/images/users/default.png");
                }
                usuario.setDetalle(detalle);

                // Rol default
                if (usuario.getId_rol() == null) {
                    Roles rol = _rolesService.findOneByField("descripcion", "USER")
                            .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));
                    usuario.setId_rol(rol.getId());
                }

                // Password, estatus y fecha
                usuario.setPassword(_passwordEncoder.encode(dto.getPassword()));
                usuario.setVerificado(true);
                usuario.setEstatus(true);
                usuario.setLastLogin(LocalDateTime.now());

                _usuarioService.save(usuario);
                usuariosGuardados.add(usuario.getUsername());

            } catch (Exception e) {
                errores.add("Usuario " + dto.getUsername() + ": " + e.getMessage());
            }
        }

        System.out.println("Usuarios guardados: " + usuariosGuardados);
        if (!errores.isEmpty()) {
            System.err.println("Errores al guardar usuarios: " + errores);
        }
    }
}
