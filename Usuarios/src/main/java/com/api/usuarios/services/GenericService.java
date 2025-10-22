package com.api.usuarios.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public abstract class GenericService<T, ID> {

    protected final MongoRepository<T, ID> repository;
    protected final MongoTemplate mongoTemplate;
    private final Class<T> entityClass;

    public GenericService(MongoRepository<T, ID> repository, MongoTemplate mongoTemplate, Class<T> entityClass) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.entityClass = entityClass;
    }

    // ---------- CRUD ----------
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    public <S extends T> S save(S entity) {
        return repository.save(entity);
    }

    public <S extends T> List<S> saveAll(List<S> entities) {
        return repository.saveAll(entities);
    }

    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    public void delete(T entity) {
        repository.delete(entity);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public long count() {
        return repository.count();
    }

    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    // ---------- BÚSQUEDAS PERSONALIZADAS ----------
    public List<T> findByField(String fieldName, Object value) {
        Query query = new Query(Criteria.where(fieldName).is(value));
        return mongoTemplate.find(query, entityClass);
    }

    public Optional<T> findOneByField(String fieldName, Object value) {
        Query query = new Query(Criteria.where(fieldName).is(value));
        return Optional.ofNullable(mongoTemplate.findOne(query, entityClass));
    }

    public List<T> findByMultipleFields(String field1, Object value1, String field2, Object value2) {
        Query query = new Query(
            new Criteria().andOperator(
                Criteria.where(field1).is(value1),
                Criteria.where(field2).is(value2)
            )
        );
        return mongoTemplate.find(query, entityClass);
    }
}
