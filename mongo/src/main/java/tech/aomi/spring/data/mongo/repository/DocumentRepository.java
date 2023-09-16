package tech.aomi.spring.data.mongo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @author Sean createAt 17-12-6.
 */
public interface DocumentRepository {

    <T> List<T> findAll(Query query, Class<T> documentClass);

    <T> List<T> findAll(Query query, Sort sort, Class<T> documentClass);

    <T> List<T> findAll(Query query, Sort sort, Class<T> resultClass, Class<?> documentClass);

    <T> List<T> findAll(Query query, Sort sort, Class<T> resultClass, String collectionName);

    /**
     * 分页查询数据
     *
     * @param query         查询条件
     * @param pageable      分页排序信息
     * @param documentClass 文档实体Class
     * @param <T>           文档类型
     * @return 分页查询结果
     */
    <T> Page<T> findAll(Query query, Pageable pageable, Class<T> documentClass);

    /**
     * 分页查询数据
     *
     * @param query         查询条件
     * @param pageable      分页排序信息
     * @param resultClass   查询结果实体类型
     * @param documentClass 文档实体Class
     * @param <T>           文档类型
     * @return 分页查询结果
     */
    <T> Page<T> findAll(Query query, Pageable pageable, Class<T> resultClass, Class<?> documentClass);

    <T> Page<T> findAll(Query query, Pageable pageable, Class<T> resultClass, String collectionName);

}
