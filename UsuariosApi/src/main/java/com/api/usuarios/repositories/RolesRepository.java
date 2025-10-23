package com.api.usuarios.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.api.usuarios.entities.roles.Roles;

@Repository
public interface RolesRepository extends MongoRepository<Roles, String> {
}