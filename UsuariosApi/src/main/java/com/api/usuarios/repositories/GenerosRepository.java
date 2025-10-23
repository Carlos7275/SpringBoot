package com.api.usuarios.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.api.usuarios.entities.generos.Generos;

@Repository
public interface GenerosRepository extends MongoRepository<Generos, String> {
}