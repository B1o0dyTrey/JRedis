package com.github.bdqfork.server.ops;

import java.util.concurrent.TimeUnit;

import com.github.bdqfork.core.operation.ValueOperation;
import com.github.bdqfork.server.database.DatabaseManager;
import com.github.bdqfork.server.transaction.OperationType;

/**
 * @author bdq
 * @since 2020/11/11
 */
public class ServerValueOperation extends AbstractServerOperation implements ValueOperation {
    @Override
    public void set(String key, Object value) {
        execute(new UpdateCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                databaseManager.saveOrUpdate(databaseId, key, value, -1L);
                return null;
            }
        });
    }

    @Override
    public void set(String key, Object value, long expire, TimeUnit timeUnit) {
        execute(new UpdateCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                databaseManager.saveOrUpdate(databaseId, key, value, expire);
                return null;
            }
        });
    }

    @Override
    public void setex(String key, Object value, long expire) {
        execute(new UpdateCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                databaseManager.saveOrUpdate(databaseId, key, value, expire * 1000);
                return null;
            }
        });
    }

    @Override
    public void setpx(String key, Object value, long expire) {
        execute(new UpdateCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                databaseManager.saveOrUpdate(databaseId, key, value, expire);
                return null;
            }
        });
    }

    @Override
    public boolean setnx(String key, Object value) {
        return (boolean) execute(new UpdateCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                if (databaseManager.get(databaseId, key) == null) {
                    databaseManager.saveOrUpdate(databaseId, key, value, -1L);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean setxx(String key, Object value) {
        return (boolean) execute(new UpdateCommand() {
            @Override
            public String getKey() {
                return key;
            }

            @Override
            public Object execute(DatabaseManager databaseManager, int databaseId) {
                if (databaseManager.get(databaseId, key) != null) {
                    databaseManager.saveOrUpdate(databaseId, key, value, -1L);
                    return true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) execute(new Command() {
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
                return databaseManager.get(databaseId, key);
            }
        });
    }

}
