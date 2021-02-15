package com.github.bdqfork.server.ops;

import com.github.bdqfork.server.transaction.TransactionManagerAware;

/**
 * @author bdq
 * @since 2020/11/11
 */
public interface ServerOperation extends TransactionManagerAware {
    void setDatabaseId(int databaseId);
}
