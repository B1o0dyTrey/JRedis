package com.github.bdqfork.server.ops;

import com.github.bdqfork.core.exception.JRedisException;
import com.github.bdqfork.core.exception.TransactionException;
import com.github.bdqfork.server.transaction.TransactionManager;

/**
 * @author bdq
 * @since 2020/11/11
 */
public class AbstractServerOperation implements ServerOperation {
    private Integer databaseId;
    private TransactionManager transactionManager;

    protected Object execute(Command command) {
        long transactionId = transactionManager.prepare(databaseId, command);
        try {
            return transactionManager.commit(transactionId);
        } catch (TransactionException e) {
            transactionManager.rollback(transactionId);
            throw new JRedisException("Command error");
        } finally {
            transactionManager.backup(transactionId);
        }
    }

    @Override
    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
