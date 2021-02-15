package com.github.bdqfork.server.ops;

import com.github.bdqfork.core.operation.KeyOperation;
import com.github.bdqfork.core.util.DateUtils;
import com.github.bdqfork.server.database.DatabaseManager;
import com.github.bdqfork.server.transaction.OperationType;

import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author bdq
 * @since 2020/11/17
 */
public class ServerKeyOperation extends AbstractServerOperation implements KeyOperation {

    @Override
    public void del(String key) {
        execute(new DeleteCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                databaseManager.delete(databaseId, key);
                return null;
            }
        });
    }

    @Override
    public void expire(String key, long expire) {
        execute(new Command() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public OperationType getOperationType() {
                return OperationType.QUERY;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                if (expire > 0) {
                    Date date = DateUtils.getDateFromNow(expire, ChronoUnit.SECONDS);
                    databaseManager.expire(databaseId, key, date.getTime());
                }
                return null;
            }
        });
    }

    @Override
    public void expireAt(String key, long expireAt) {
        execute(new Command() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public OperationType getOperationType() {
                return OperationType.QUERY;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                databaseManager.expire(databaseId, key, expireAt);
                return null;
            }
        });
    }

    @Override
    public Long ttl(String key) {
        return (Long) execute(new Command() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public OperationType getOperationType() {
                return OperationType.QUERY;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                Long ttl = databaseManager.ttl(databaseId, key);
                if (ttl == null) {
                    ttl = -1L;
                }
                return null;
            }
        });
    }

    @Override
    public Long ttlAt(String key) {
        return (Long) execute(new Command() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public OperationType getOperationType() {
                return OperationType.QUERY;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                Long ttlAt = databaseManager.ttlAt(databaseId, key);
                if (ttlAt == null) {
                    ttlAt = -1L;
                }
                return ttlAt;
            }
        });
    }
}
