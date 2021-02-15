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
class Database {
    private final Map<String, Object> dictMap;
    private final Map<String, Long> expireMap;

    public Database() {
        this(new ConcurrentHashMap<>(256), new ConcurrentHashMap<>(256));
    }

    private Database(Map<String, Object> dictMap, Map<String, Long> expireMap) {
        this.dictMap = dictMap;
        this.expireMap = expireMap;
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
        if (ttl(key) == -1 || ttl(key) > 0) {
            return dictMap.get(key);
        } else {
            return null;
        }
    }

    /**
     * 查询数据剩余过期时间， 如果key不存在，返回-1， 如果key存在，且已经过期，返回-2， 否则返回剩余时间
     *
     * @param key 键
     */
    public Long ttl(String key) {
        if (expireMap.containsKey(key)) {
            long ttl = expireMap.get(key) - System.currentTimeMillis();
            if (ttl <= 0) {
                dictMap.remove(key);
                expireMap.remove(key);
                return -2L;
            }
            return ttl;
        } else {
            return -1L;
        }
    }

    /**
     * 查询数据过期时间，-1表示查询的数据已经过期或者不存在
     *
     * @param key 键
     */
    public Long ttlAt(String key) {
        if (ttl(key) > 0) {
            return expireMap.get(key);
        }
        return -1L;
    }

    public void expire(String key, long expire) {
        expireMap.put(key, expire);
    }

    public Database dump() {
        Map<String, Object> dictMapDump = new ConcurrentHashMap<>(this.dictMap.size());
        dictMapDump.putAll(this.dictMap);
        Map<String, Long> expireMapDump = new ConcurrentHashMap<>(this.expireMap.size());
        expireMapDump.putAll(expireMapDump);
        return new Database(dictMapDump, expireMapDump);
    }

}
