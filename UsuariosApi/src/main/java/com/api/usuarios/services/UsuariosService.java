package com.api.usuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.api.usuarios.entities.usuarios.Usuarios;
import com.api.usuarios.models.Dto.Usuarios.CambiarPasswordDTO;

@Service

public class UsuariosService extends GenericService<Usuarios, String> {

    @Autowired
    private BCryptPasswordEncoder _passwordEncoder;

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

    public void CambiarPassword(Usuarios usuario, CambiarPasswordDTO dto) {
        // Validar que las nuevas contraseñas coincidan
        if (!dto.getPasswordNueva().equals(dto.getPasswordConfirmar())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las nuevas contraseñas no coinciden");
        }

        // Validar contraseña actual
        if (!_passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }

        // Actualizar contraseña
        usuario.setPassword(_passwordEncoder.encode(dto.getPasswordNueva()));
        repository.save(usuario);
    }

}
