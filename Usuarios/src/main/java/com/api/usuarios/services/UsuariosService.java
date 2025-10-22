package com.api.usuarios.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.api.usuarios.entities.usuarios.Usuarios;

@Service
public class UsuariosService extends GenericService<Usuarios, String> {

    public UsuariosService(MongoRepository<Usuarios, String> repository, MongoTemplate mongoTemplate) {
        super(repository, mongoTemplate, Usuarios.class);
    }

    public Usuarios buscarPorCorreo(String correo){
        return findOneByField("correo", correo).orElse(null);
    }

}
