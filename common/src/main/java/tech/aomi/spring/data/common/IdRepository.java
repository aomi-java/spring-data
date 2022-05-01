package tech.aomi.spring.data.common;


/**
 * id 仓库
 *
 * @author Sean createAt 17-12-26.
 */
public interface IdRepository {

    /**
     * 创建seq
     *
     * @param sequenceName 序列名称
     */
    void createSequence(String sequenceName);

    /**
     * 根绝序列名称获取id值
     *
     * @param sequenceName 序列名称
     * @return value
     */
    Long generateSequence(String sequenceName);

}
