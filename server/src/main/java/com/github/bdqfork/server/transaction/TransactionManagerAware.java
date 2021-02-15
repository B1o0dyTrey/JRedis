package com.github.bdqfork.server.transaction;

/**
 * @author bdq
 * @since 2020/11/11
 */
public interface TransactionManagerAware {
    void setTransactionManager(TransactionManager transactionManager);
}
