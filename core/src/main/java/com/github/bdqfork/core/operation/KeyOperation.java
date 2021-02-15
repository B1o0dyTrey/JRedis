package com.github.bdqfork.core.operation;

/**
 * @author bdq
 * @since 2020/11/17
 */
public interface KeyOperation extends Operation {
    /**
     * 删除一个键值对
     *
     * @param key 键
     */
    void del(String key);

    /**
     * 给一个键添加或者更新过期时间
     *
     * @param key    键
     * @param expire 过期时间，单位秒
     */
    void expire(String key, long expire);

    /**
     * 给一个键添加或者更新过期时间
     *
     * @param key      键
     * @param expireAt unixTime
     */
    void expireAt(String key, long expireAt);

    /**
     * 返回剩余过期时间
     *
     * @param key 键
     * @return Long
     */
    Long ttl(String key);

    /**
     * 返回过期时间，unix time
     *
     * @param key 键
     * @return Long
     */
    Long ttlAt(String key);
}
