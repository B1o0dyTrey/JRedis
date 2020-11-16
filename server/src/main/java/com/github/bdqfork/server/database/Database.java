package com.github.bdqfork.server.database;

import com.github.bdqfork.core.util.DateUtils;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据存储，对数据进行管理，负责增删改查，同时负责淘汰过期数据
 *
 * @author bdq
 * @since 2020/9/20
 */
public class Database {
    private final Map<String, Object> dictMap;
    private final Map<String, Long> expireMap;

    public Database() {
        dictMap = new ConcurrentHashMap<>(256);
        expireMap = new ConcurrentHashMap<>(256);
    }

    /**
     * 删除数据
     *
     * @param key 键
     */
    public void delete(String key) {
        dictMap.remove(key);
        expireMap.remove(key);
    }

    /**
     * 更新数据
     *
     * @param key   键
     * @param value 值
     */
    public void saveOrUpdate(String key, Object value, Long expire) {
        dictMap.put(key, value);
        if (expire > 0) {
            Date date = DateUtils.getDateFromNow(expire, ChronoUnit.MILLIS);
            expireMap.put(key, date.getTime());
        }
    }


    /**
     * 查询数据
     *
     * @param key 键
     */
    public Object get(String key) {
        if (!dictMap.containsKey(key)) {
            return null;
        }
        Long ttl = ttl(key);
        if (ttl == null || ttl > 0) {
            return dictMap.get(key);
        } else {
            dictMap.remove(key);
            expireMap.remove(key);
            return null;
        }
    }

    /**
     * 查询数据剩余过期时间
     *
     * @param key 键
     */
    public Long ttl(String key) {
        if (expireMap.containsKey(key)) {
            return expireMap.get(key) - System.currentTimeMillis();
        } else {
            return null;
        }
    }

    /**
     * 查询数据过期时间
     *
     * @param key 键
     */
    public Long ttlAt(String key) {
        return expireMap.get(key);
    }
}
