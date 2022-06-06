package tech.aomi.spring.data.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @author 田尘殇
 */
@Repository
@Transactional(readOnly = true)
@SuppressWarnings("unchecked")
public class EntityRepositoryImpl implements EntityRepository {

    public static final String COUNT_SELECT = "SELECT count(*) ";

    private static final Pattern WHERE_PATTERN = Pattern.compile("where", Pattern.CASE_INSENSITIVE);

    /**
     * 启用删除逻辑判断
     */
    private final Boolean enabledSoftDelete;

    /**
     * 删除字段名
     */
    private final String softDeleteFieldName;

    /**
     * 标识未删除的数据值
     */
    private final Object notDeletedValue;


    private EntityManager entityManager;

    public EntityRepositoryImpl(EntityManager entityManager) {
        this(entityManager, false);
    }

    public EntityRepositoryImpl(EntityManager entityManager, Boolean enabledSoftDelete) {
        this(entityManager, enabledSoftDelete, "deleted", false);
    }

    public EntityRepositoryImpl(EntityManager entityManager, Boolean enabledSoftDelete, String softDeleteFieldName, Object notDeletedValue) {
        this.entityManager = entityManager;
        this.enabledSoftDelete = enabledSoftDelete;
        this.softDeleteFieldName = softDeleteFieldName;
        this.notDeletedValue = notDeletedValue;
    }

    @Override
    @Transactional
    public void save(Object... entities) {
        for (Object entity : entities) {
            entityManager.persist(entity);
        }
    }

    /**
     * 保存或者更新数据
     *
     * @param entities 数据表
     */
    @Override
    @Transactional
    public void save(Iterable<?> entities) {
        entities.forEach(this::save);
    }


    @Override
    @Transactional
    public void update(Object... entities) {
        for (Object entity : entities) {
            entityManager.merge(entity);
        }
    }

    @Override
    @Transactional
    public void update(Iterable<?> entities) {
        entities.forEach(this::update);
    }

    @Override
    @Transactional
    public void delete(Class<?> domainClass, Object id) {
        Assert.notNull(id, "id 不能为null");
        Object entity = findOne(domainClass, id);
        if (entity == null) {
            return;
        }
        delete(entity);
    }

    /**
     * 删除数据
     *
     * @param entities 数据表
     */
    @Override
    @Transactional
    public void delete(Object... entities) {
        for (Object entity : entities) {
            this.entityManager.remove(this.entityManager.contains(entity) ? entity : this.entityManager.merge(entity));
        }
    }

    /**
     * 删除数据
     *
     * @param entities 数据表
     */
    @Override
    @Transactional
    public void delete(Iterable<?> entities) {
        entities.forEach(this::delete);
    }

    @Override
    @Transactional
    public <T> void delete(Class<T> domainClass, Specification<T, T> specification) {
        delete(findAll(domainClass, specification));
    }

    /**
     * 根据传入的JPQL语句查询数据
     *
     * @param jpql   jpql
     * @param params 参数
     * @return 查询结果
     */
    @Override
    public <T> List<T> query(String jpql, Object[] params) {
        Query query = entityManager.createQuery(jpql);
        setQueryParams(query, params);
        return query.getResultList();
    }

    /**
     * 根据传入的JPQL语句查询数据
     *
     * @param jpql   jpql
     * @param params 参数
     * @return 查询结果
     */
    @Override
    public <T> List<T> query(String jpql, Map<String, Object> params) {
        Query query = entityManager.createQuery(jpql);
        setQueryParams(query, params);
        return query.getResultList();
    }

    /**
     * 根据传入的JPQL 语句执行查询,返回第一条记录
     *
     * @param jpql   jpql
     * @param params 查询参数
     * @return 查询结果
     */
    @Override
    public <T> T queryOne(String jpql, Object... params) {
        Query query = entityManager.createQuery(jpql);
        setQueryParams(query, params);
        return executeQueryOne(query);
    }

    /**
     * 根据传入的JPQL 语句执行查询,返回第一条记录
     *
     * @param jpql   jpql
     * @param params 查询参数
     * @return 查询结果
     */
    @Override
    public <T> T queryOne(String jpql, Map<String, Object> params) {
        Query query = entityManager.createQuery(jpql);
        setQueryParams(query, params);
        return executeQueryOne(query);
    }

    @Override
    public <T> Page<T> query(String select, String from, Pageable pageable, String alias, Object[] params) {
        String fromSql = from;
        if (null != pageable) {
            fromSql = QueryUtils.applySorting(from, pageable.getSort(), alias);
        }
        Query query = entityManager.createQuery(select + " " + fromSql);
        setQueryParams(query, params);

        return null == pageable ? new PageImpl<T>(query.getResultList()) : readPage(from, query, pageable, params);
    }

    /**
     * 根据传入的JPQL语句查询数据
     *
     * @param select   jpql语句select部分
     * @param from     jpql 语句 from部分
     * @param pageable 分页信息
     * @param params   参数
     * @return 返回分页查询结果
     */
    @Override
    public <T> Page<T> query(String select, String from, Pageable pageable, Map<String, Object> params) {
        return query(select, from, pageable, params, null);
    }

    @Override
    public <T> Page<T> query(String select, String from, Pageable pageable, Map<String, Object> params, String alias) {
        String fromSql = from;
        if (null != pageable) {
            fromSql = QueryUtils.applySorting(from, pageable.getSort(), alias);
        }

        Query query = entityManager.createQuery(select + fromSql);
        setQueryParams(query, params);

        return null == pageable ? new PageImpl<T>(query.getResultList()) : readPage(from, query, pageable, params);
    }

    @Override
    public <T> Long countQuery(Class<T> domainClass) {
        return countQuery(domainClass, null);
    }

    @Override
    public <T> Long countQuery(Class<T> domainClass, Specification<T, Long> specification) {
        return executeCountQuery(getCountQuery(domainClass, specification));
    }

    /**
     * 通过ID 查询数据
     *
     * @param domainClass 表实体类Class
     * @param id          id 值
     * @return 查询的结果
     */
    @Override
    public <T> T findOne(Class<T> domainClass, Object id) {
        return entityManager.find(domainClass, id);
    }

    @Override
    public <T> T findOne(Class<T> domainClass, Specification<T, T> specification) {
        try {
            return getQuery(true, domainClass, domainClass, specification, (Sort) null).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public <T> T findOneWithRandom(Class<T> domainClass, Specification<T, T> specification) {
        long count = countQuery(domainClass, (Specification<T, Long>) specification);
        if (count == 0)
            return null;
        TypedQuery<T> query = getQuery(true, domainClass, domainClass, specification, (Sort) null);
        Random random = new Random();
        int first = random.nextInt((int) count);
        query.setFirstResult(first);
        query.setMaxResults(1);
        return query.getSingleResult();
    }

    @Override
    public <T> List<T> findAll(Class<T> domainClass) {
        return findAll(domainClass, null);
    }

    @Override
    public <T> List<T> findAll(Class<T> domainClass, Specification<T, T> specification) {
        return findAll(domainClass, specification, (Sort) null);
    }

    @Override
    public <T> List<T> findAll(Class<T> domainClass, Specification<T, T> specification, Sort sort) {
        return getQuery(true, domainClass, domainClass, specification, sort).getResultList();
    }

    @Override
    public <T> Page<T> findAll(Class<T> domainClass, Specification<T, T> spec, Pageable pageable) {
        TypedQuery<T> query = getQuery(true, domainClass, domainClass, spec, pageable);
        return pageable == null ? new PageImpl<>(query.getResultList()) : readPage(domainClass, query, pageable, spec);
    }

    @Override
    public <T> List<T> findAllCustomSelect(Class<T> domainClass, Specification<T, T> specification) {
        return findAllCustomSelect(domainClass, domainClass, specification);
    }

    @Override
    public <T> List<T> findAllCustomSelect(Class<T> domainClass, Specification<T, T> specification, Sort sort) {
        return findAllCustomSelect(domainClass, domainClass, specification, sort);
    }

    @Override
    public <T> Page<T> findAllCustomSelect(Class<T> domainClass, Specification<T, T> specification, Pageable pageable) {
        return findAllCustomSelect(domainClass, domainClass, specification, pageable);
    }

    @Override
    public <T, R> List<R> findAllCustomSelect(Class<T> domainClass, Class<R> resultClass, Specification<T, R> specification) {
        return findAllCustomSelect(domainClass, resultClass, specification, (Sort) null);
    }

    @Override
    public <T, R> List<R> findAllCustomSelect(Class<T> domainClass, Class<R> resultClass, Specification<T, R> specification, Sort sort) {
        return getQuery(false, domainClass, resultClass, specification, sort).getResultList();
    }

    @Override
    public <T, R> Page<R> findAllCustomSelect(Class<T> domainClass, Class<R> resultClass, Specification<T, R> specification, Pageable pageable) {
        TypedQuery query = getQuery(false, domainClass, resultClass, specification, pageable);
        return pageable == null ? new PageImpl<>(query.getResultList()) : readPage(domainClass, query, pageable, specification);
    }

    @Override
    public <T> T findOneCustomSelect(Class<T> domainClass, Specification<T, T> specification) {
        return findOneCustomSelect(domainClass, domainClass, specification);
    }

    @Override
    public <T> T findOneCustomSelect(Class<T> domainClass, Specification<T, T> specification, Sort sort) {
        return findOneCustomSelect(domainClass, domainClass, specification, sort);
    }

    @Override
    public <T, R> R findOneCustomSelect(Class<T> domainClass, Class<R> resultClass, Specification<T, R> specification) {
        return findOneCustomSelect(domainClass, resultClass, specification, null);
    }

    @Override
    public <T, R> R findOneCustomSelect(Class<T> domainClass, Class<R> resultClass, Specification<T, R> specification, Sort sort) {
        try {
            return getQuery(false, domainClass, resultClass, specification, sort).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public int executeUpdate(String updateJpql, Object... params) {
        Query query = this.entityManager.createQuery(updateJpql);
        setQueryParams(query, params);
        return query.executeUpdate();
    }

    @Override
    public int executeUpdateInBatch(String updateJpql, Object... params) {
        return executeUpdate(updateJpql, params);
    }

    @Override
    @Transactional
    public int executeUpdate(String updateJpql, Map<String, Object> params) {
        Query query = this.entityManager.createQuery(updateJpql);
        setQueryParams(query, params);
        return query.executeUpdate();
    }

    @Override
    public <T> boolean exists(Class<T> domainClass, Specification<T, Long> specification) {
        Long count = countQuery(domainClass, specification);
        return null != count && count > 0;
    }

    @Override
    public Object getNative() {
        return this.entityManager;
    }

    private <T> Page<T> readPage(String from, Query query, Pageable pageable, Object[] params) {
        Long total = executeCountQuery(getCountQuery(from, params));
        return executeReadPage(query, pageable, total);
    }

    private <T> Page<T> readPage(String from, Query query, Pageable pageable, Map<String, Object> params) {
        Long total = executeCountQuery(getCountQuery(from, params));
        return executeReadPage(query, pageable, total);
    }

    private <T> Page<T> readPage(Class<T> domainClass, TypedQuery<T> query, Pageable pageable, Specification spec) {
        Long total = executeCountQuery(getCountQuery(domainClass, spec));
        return executeReadPage(query, pageable, total);
    }

    private <T> Page<T> executeReadPage(Query query, Pageable pageable, Long total) {
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<T> content = total > pageable.getOffset() ? query.getResultList() : Collections.emptyList();
        if (content == null)
            content = Collections.emptyList();

        return new PageImpl<>(content, pageable, total);
    }

    private TypedQuery<Long> getCountQuery(String from, Object[] params) {
        TypedQuery<Long> query = entityManager.createQuery(COUNT_SELECT + from, Long.class);
        setQueryParams(query, params);
        return query;
    }

    private TypedQuery<Long> getCountQuery(String from, Map<String, Object> params) {
        TypedQuery<Long> query = entityManager.createQuery(COUNT_SELECT + from, Long.class);
        setQueryParams(query, params);
        return query;
    }

    private <T> TypedQuery<Long> getCountQuery(Class<T> domainClass, Specification<T, Long> spec) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<T> root = applySpecificationToCriteria(domainClass, spec, query);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        return entityManager.createQuery(query);
    }

    private <T, R> TypedQuery<R> getQuery(boolean selectAll, Class<T> domainClass, Class<R> resultClass, Specification<T, R> specification, Sort sort) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> query = builder.createQuery(resultClass);

        Root root = applySpecificationToCriteria(domainClass, specification, query);
        if (selectAll) {
            query.select(root);
        }
        if (sort != null) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }
        return entityManager.createQuery(query);
    }

    private <T, R> TypedQuery<R> getQuery(boolean selectAll, Class<T> domainClass, Class<R> resultClass, Specification<T, R> spec, Pageable pageable) {
        Sort sort = pageable == null ? null : pageable.getSort();
        return getQuery(selectAll, domainClass, resultClass, spec, sort);
    }

    private <T, R> Root<T> applySpecificationToCriteria(Class<T> domainClass, Specification<T, R> specification, CriteriaQuery<R> query) {
        Root<T> root = query.from(domainClass);

        if (specification == null) {
            return root;
        }

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        Predicate predicate = specification.toPredicate(root, query, builder);

        List<Predicate> predicates = new ArrayList<>();
        // 添加用户设置的查询条件
        if (null != predicate) {
            predicates.add(predicate);
        }

        // 排除删除的字段
        if (this.enabledSoftDelete) {
            predicates.add(builder.equal(root.get(softDeleteFieldName), notDeletedValue));
        }
        if (predicates.size() > 0) {
            query.where(builder.and(predicates.toArray(new Predicate[0])));
        }
        return root;
    }

    /**
     * Executes a count query and transparently sums up all values returned.
     *
     * @param query must not be {@literal null}.
     * @return Long
     */
    private Long executeCountQuery(TypedQuery<Long> query) {
        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }

    private <T> T executeQueryOne(Query query) {
        T result = null;
        try {
            result = (T) query.getSingleResult();
        } catch (Exception ignored) {
        }
        return result;
    }

    private void setQueryParams(Query query, Object[] params) {
        int i = 0;
        for (Object param : params) {
            query.setParameter(i++, param);
        }
    }

    private void setQueryParams(Query query, Map<String, Object> params) {
        for (String key : params.keySet()) {
            query.setParameter(key, params.get(key));
        }
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
