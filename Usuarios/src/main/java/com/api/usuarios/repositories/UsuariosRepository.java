package com.api.usuarios.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.api.usuarios.entities.usuarios.Usuarios;

@Repository
public interface UsuariosRepository extends MongoRepository<Usuarios, String> {

  public   Usuarios findByCorreo(String correo);

}