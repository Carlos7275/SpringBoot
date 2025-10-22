package com.api.usuarios.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.api.usuarios.entities.generos.Generos;

@Service
public class GenerosService extends GenericService<Generos, String> {

    public GenerosService(MongoRepository<Generos, String> repository, MongoTemplate mongoTemplate) {
        super(repository, mongoTemplate, Generos.class);
    }

}
