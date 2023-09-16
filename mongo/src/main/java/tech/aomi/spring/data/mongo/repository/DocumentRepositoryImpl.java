package tech.aomi.spring.data.mongo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @author Sean createAt 17-12-6.
 */
public class DocumentRepositoryImpl implements DocumentRepository {

    private final MongoTemplate mongoTemplate;

    public DocumentRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public <T> List<T> findAll(Query query, Class<T> documentClass) {
        return this.mongoTemplate.find(query, documentClass);
    }

    @Override
    public <T> List<T> findAll(Query query, Sort sort, Class<T> documentClass) {
        return findAll(query, sort, documentClass, documentClass);
    }

    @Override
    public <T> List<T> findAll(Query query, Sort sort, Class<T> resultClass, Class<?> documentClass) {
        return findAll(query, sort, resultClass, mongoTemplate.getCollectionName(documentClass));
    }

    @Override
    public <T> List<T> findAll(Query query, Sort sort, Class<T> resultClass, String collectionName) {
        query.with(sort);
        return this.mongoTemplate.find(query, resultClass, collectionName);
    }

    @Override
    public <T> Page<T> findAll(Query query, Pageable pageable, Class<T> documentClass) {
        long total = 0;
        boolean noCount = true;
        if (null != pageable) {
            total = this.mongoTemplate.count(query, documentClass);
            noCount = false;
            query.with(pageable);
        }
        List<T> content = this.mongoTemplate.find(query, documentClass);
        if (noCount) {
            total = content.size();
        }

        return pageable == null ? new PageImpl<>(content) : new PageImpl<>(content, pageable, total);
    }


    @Override
    public <T> Page<T> findAll(Query query, Pageable pageable, Class<T> resultClass, Class<?> documentClass) {
        return findAll(query, pageable, resultClass, mongoTemplate.getCollectionName(documentClass));
    }

    @Override
    public <T> Page<T> findAll(Query query, Pageable pageable, Class<T> resultClass, String collectionName) {
        long total = 0;
        boolean noCount = true;
        if (null != pageable) {
            total = this.mongoTemplate.count(query, collectionName);
            noCount = false;
            query.with(pageable);
        }
        List<T> content = this.mongoTemplate.find(query, resultClass, collectionName);
        if (noCount) {
            total = content.size();
        }

        return pageable == null ? new PageImpl<>(content) : new PageImpl<>(content, pageable, total);
    }


}
