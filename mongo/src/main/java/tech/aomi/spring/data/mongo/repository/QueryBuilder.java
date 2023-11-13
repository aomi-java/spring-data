package tech.aomi.spring.data.mongo.repository;

import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Sean createAt 17-12-13.
 */
public class QueryBuilder {

    @Getter
    private final List<Criteria> criteria = new ArrayList<>();
    private final List<String> excludes = new ArrayList<>();
    private final List<String> includes = new ArrayList<>();
    private Sort sort;
    private Pageable pageable;

    private String positionKey;
    private int positionValue;

    private QueryBuilder() {
//        query = new Query();
    }

    public static QueryBuilder builder() {
        return new QueryBuilder();
    }

    public QueryBuilder is(String k, Object v) {
        this.criteria.add(Criteria.where(k).is(v));
        return this;
    }

    public QueryBuilder in(String field, Object... v) {
        this.criteria.add(Criteria.where(field).in(v));
        return this;
    }

    public QueryBuilder in(String field, Collection<?> v) {
        this.criteria.add(Criteria.where(field).in(v));
        return this;
    }

    public QueryBuilder nin(String field, Object... v) {
        this.criteria.add(Criteria.where(field).nin(v));
        return this;
    }

    public QueryBuilder nin(String field, Collection<?> v) {
        this.criteria.add(Criteria.where(field).nin(v));
        return this;
    }

    public QueryBuilder regex(String field, String regex) {
        this.criteria.add(Criteria.where(field).regex(regex));
        return this;
    }

    public QueryBuilder regex(String field, Pattern regex) {
        this.criteria.add(Criteria.where(field).regex(regex));
        return this;
    }

    public QueryBuilder like(String key, String value) {
        regex(key, ".*" + value + ".*");
        return this;
    }

    public QueryBuilder leftLike(String key, String value) {
        this.criteria.add(Criteria.where(key).regex(".*" + value));
        return this;
    }

    public QueryBuilder rightLike(String key, String value) {
        this.criteria.add(Criteria.where(key).regex(value + ".*"));
        return this;
    }

    public QueryBuilder gt(String field, Object v) {
        this.criteria.add(Criteria.where(field).gt(v));
        return this;
    }

    public QueryBuilder gte(String field, Object v) {
        this.criteria.add(Criteria.where(field).gte(v));
        return this;
    }

    public QueryBuilder lt(String field, Object v) {
        this.criteria.add(Criteria.where(field).lt(v));
        return this;
    }

    public QueryBuilder lte(String field, Object v) {
        this.criteria.add(Criteria.where(field).lte(v));
        return this;
    }

    public QueryBuilder between(String field, Object v1, Object v2) {
        this.criteria.add(Criteria.where(field).gte(v1).lte(v2));
        return this;
    }

    public QueryBuilder or(Criteria... criteria) {
        this.criteria.add(new Criteria().orOperator(criteria));
        return this;
    }

    public QueryBuilder and(Criteria... criteria) {
        this.criteria.addAll(Arrays.stream(criteria).toList());
        return this;
    }

    public QueryBuilder nor(Criteria... criteria) {
        this.criteria.add(new Criteria().norOperator(criteria));
        return this;
    }

    public QueryBuilder not(String field) {
        this.criteria.add(Criteria.where(field).not());
        return this;
    }

    public QueryBuilder ne(String field, Object o) {
        this.criteria.add(Criteria.where(field).ne(o));
        return this;
    }


    public QueryBuilder exists(String key, boolean exists) {
        this.criteria.add(Criteria.where(key).exists(exists));
        return this;
    }

    public QueryBuilder position(String field, int value) {
//        Field f = query.fields();
//        f.position(field, value);
        this.positionKey = field;
        this.positionValue = value;
        return this;
    }

    public QueryBuilder exclude(String... keys) {
        this.excludes.addAll(Arrays.stream(keys).toList());
//        Field f = query.fields();
//        for (String key : keys) {
//            f.exclude(key);
//        }
        return this;
    }

    public QueryBuilder include(String... keys) {
        this.includes.addAll(Arrays.stream(keys).toList());
//        Field f = query.fields();
//        for (String key : keys) {
//            f.include(key);
//        }
        return this;
    }

    public QueryBuilder sort(Sort sort) {
        this.sort = sort;
//        query.with(sort);
        return this;
    }

    public QueryBuilder page(Pageable pageable) {
        this.pageable = pageable;
//        query.with(pageable);
        return this;
    }

//    public QueryBuilder addCriteria(CriteriaDefinition criteriaDefinition) {
//        this.criteria.add(criteriaDefinition);
//        return this;
//    }

    public Query build() {
        var query = new Query();
        criteria.forEach(query::addCriteria);
        Field f = query.fields();
        if (null != positionKey) {
            f.position(positionKey, positionValue);
        }
        f.exclude(excludes.toArray(new String[0]));
        f.include(includes.toArray(new String[0]));
        if (null != sort) {
            query.with(sort);
        }
        if (null != pageable) {
            query.with(pageable);
        }


        return query;
    }

}
