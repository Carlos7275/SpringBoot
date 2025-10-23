package com.api.usuarios.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
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
                        Criteria.where(field2).is(value2)));
        return mongoTemplate.find(query, entityClass);
    }

    // ---------- Paginación Avanzada ----------
    public record Filter(String field, String operator, Object value) {
    }

    public record PaginateResult<T>(List<T> data, long total) {
    }

    /**
     * Paginación avanzada con búsqueda, filtros y orden.
     *
     * @param page         Página (1-indexed)
     * @param limit        Cantidad por página
     * @param search       String de búsqueda
     * @param searchFields Campos donde buscar
     * @param filters      Lista de filtros {field, operator, value}
     * @param order        Map de {campo: ASC|DESC}
     */
    public PaginateResult<T> paginate(
            int page,
            int limit,
            String search,
            List<String> searchFields,
            List<Filter> filters,
            Map<String, String> order) {
        Query query = new Query();

        // --- FILTROS ---
        if (filters != null && !filters.isEmpty()) {
            List<Criteria> criteriaList = new ArrayList<>();
            for (Filter f : filters) {
                switch (f.operator.toLowerCase()) {
                    case "eq" -> criteriaList.add(Criteria.where(f.field).is(f.value));
                    case "lt" -> criteriaList.add(Criteria.where(f.field).lt(f.value));
                    case "lte" -> criteriaList.add(Criteria.where(f.field).lte(f.value));
                    case "gt" -> criteriaList.add(Criteria.where(f.field).gt(f.value));
                    case "gte" -> criteriaList.add(Criteria.where(f.field).gte(f.value));
                    case "like" -> criteriaList.add(Criteria.where(f.field).regex(".*" + f.value + ".*", "i"));
                    case "in" -> {
                        if (f.value instanceof Collection<?> col) {
                            criteriaList.add(Criteria.where(f.field).in(col));
                        }
                    }
                }
            }
            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            }
        }

        // --- BÚSQUEDA GLOBAL ---
        if (search != null && !search.isBlank() && searchFields != null && !searchFields.isEmpty()) {
            List<Criteria> searchCriteria = searchFields.stream()
                    .map(f -> Criteria.where(f).regex(".*" + search + ".*", "i"))
                    .collect(Collectors.toList());
            query.addCriteria(new Criteria().orOperator(searchCriteria.toArray(new Criteria[0])));
        }

        // --- ORDENAMIENTO ---
        if (order != null && !order.isEmpty()) {
            List<Sort.Order> sortOrders = order.entrySet().stream()
                    .map(e -> new Sort.Order(
                            "DESC".equalsIgnoreCase(e.getValue()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                            e.getKey()))
                    .toList();
            query.with(Sort.by(sortOrders));
        } else {
            query.with(Sort.by(Sort.Direction.ASC, "id"));
        }

        // --- PAGINACIÓN ---
        Pageable pageable = PageRequest.of(page - 1, limit);
        query.with(pageable);

        // --- EJECUCIÓN ---
        List<T> data = mongoTemplate.find(query, entityClass);
        long total = mongoTemplate.count(query.skip(0).limit(0), entityClass); // contar total sin paginar

        return new PaginateResult<>(data, total);
    }

}
