package com.api.usuarios.services;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import com.api.usuarios.entities.roles.Roles;
@Service
public class RolesService extends GenericService<Roles, String> {

    public RolesService(MongoRepository<Roles, String> repository, MongoTemplate mongoTemplate) {
        super(repository, mongoTemplate, Roles.class);
    }

}
