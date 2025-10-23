package com.api.usuarios.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.usuarios.entities.usuarios.Usuarios;

@Service

public class UsuariosService extends GenericService<Usuarios, String> {

    public UsuariosService(MongoRepository<Usuarios, String> repository, MongoTemplate mongoTemplate) {
        super(repository, mongoTemplate, Usuarios.class);
    }

    public Usuarios buscarPorCorreo(String correo) {
        return findOneByField("correo", correo).orElse(null);
    }

    public Boolean cambiarEstatus(String id) {
        Usuarios usuario = findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Usuarios usuarioLogueado = (Usuarios) authentication.getPrincipal();

        if (usuario.getId().equals(usuarioLogueado.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes cambiar tu propio estatus");
        }
        Boolean nuevoEstatus = !usuario.getEstatus();
        usuario.setEstatus(nuevoEstatus);

        repository.save(usuario);

        return nuevoEstatus;
    }

}
