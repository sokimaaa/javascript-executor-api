package com.sokima.executor.persistence;

import com.sokima.executor.model.search.FilterParam;
import com.sokima.executor.model.search.SearchParamHolder;
import com.sokima.executor.model.search.SortingOrderEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractExecutorMongoTemplate<E> implements ExecutorMongoTemplate<E> {

    protected final MongoTemplate mongoTemplate;

    @Override
    public List<E> findBy(String field, Object value, SearchParamHolder searchParam) {
        var query = new Query();
        Criteria mainFilterCriteria = Criteria.where(field).is(value);

        var sort = transformSortParams(searchParam.sortParams());
        var pageable = PageRequest.of(searchParam.pageCount(), searchParam.pageSize(), sort);

        query.with(pageable);

        List<Criteria> criteria = new ArrayList<>();
        for (FilterParam filterParam : searchParam.filterParams()) {
            String filter = filterParam.filter();
            String filterValue = filterParam.value();

            Criteria where = Criteria.where(filter);
            switch (filterParam.operator()) {
                case GTE -> where.gte(filterValue);
                case EQ -> where.is(filterValue);
                case LTE -> where.lte(filterValue);
                default -> throw new UnsupportedOperationException("Provided criteria does not support.");
            }
            criteria.add(where);
        }

        query.addCriteria(mainFilterCriteria.andOperator(criteria.toArray(Criteria[]::new)));
        return mongoTemplate.find(query, getGenericClass(), getCollectionName());
    }

    @Override
    public Optional<E> findById(Long id) {
        var query = new Query();
        Criteria idSearchCriteria = Criteria.where(getIdName()).is(id);
        query.addCriteria(idSearchCriteria);
        return mongoTemplate.find(query, getGenericClass(), getCollectionName()).stream().findAny();
    }

    @Override
    public void insert(E element) {
        mongoTemplate.insert(element, getCollectionName());
    }

    @Override
    public void update(E element) {
        mongoTemplate.save(element, getCollectionName());
    }

    @Override
    public void delete(E element) {
        mongoTemplate.remove(element, getCollectionName());
    }

    protected abstract String getCollectionName();

    protected abstract String getIdName();

    protected abstract Class<E> getGenericClass();

    private Sort transformSortParams(Map<String, SortingOrderEnum> sortParams) {
        String[] properties = sortParams.keySet().toArray(String[]::new);
        Sort.Direction direction = sortParams.values()
                .stream()
                .findAny()
                .map(x -> Sort.Direction.valueOf(x.name()))
                .orElse(Sort.Direction.ASC);
        return Sort.by(direction, properties);
    }
}
