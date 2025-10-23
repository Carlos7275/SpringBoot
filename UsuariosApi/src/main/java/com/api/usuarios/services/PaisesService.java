package com.api.usuarios.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.api.usuarios.entities.paises.Paises;

@Service
public class PaisesService extends GenericService<Paises, String> {

    public PaisesService(MongoRepository<Paises, String> repository, MongoTemplate mongoTemplate) {
        super(repository, mongoTemplate, Paises.class);
    }

}
