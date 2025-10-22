package com.api.usuarios.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.api.usuarios.entities.paises.Paises;

@Repository
public interface PaisesRepository extends MongoRepository<Paises, String> {
}