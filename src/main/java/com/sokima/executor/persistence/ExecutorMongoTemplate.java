package com.sokima.executor.persistence;

import com.sokima.executor.model.search.SearchParamHolder;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Adapter for using {@link MongoTemplate} for specific collection.
 */
public interface ExecutorMongoTemplate<E> {

    void insert(E element);

    void update(E element);

    void delete(E element);

    Optional<E> findById(Long id);

    List<E> findBy(String field, Object value, SearchParamHolder searchParam);
}
