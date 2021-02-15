package com.github.bdqfork.server.transaction;

import java.io.Serializable;

/**
 * 记录已经成功的操作
 *
 * @author bdq
 * @since 2020/09/21
 */
public class RedoLog implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 数据库id
     */
    private Integer databaseId;
    /**
     * key
     */
    private String key;
    /**
     * value
     */
    private Object value;
    /**
     * 操作类型
     */
    private OperationType operationType;
    /**
     * 过期时间
     */
    private Long expireAt;

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }
}
