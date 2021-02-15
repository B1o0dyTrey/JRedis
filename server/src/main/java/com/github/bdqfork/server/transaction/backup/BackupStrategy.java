package com.github.bdqfork.server.transaction.backup;

import com.github.bdqfork.server.database.DatabaseManager;
import com.github.bdqfork.server.transaction.TransactionLog;

/**
 * 备份策略
 *
 * @author bdq
 * @since 2020/09/22
 */
public interface BackupStrategy {
    /**
     * 以一个事物为单位备份日志
     * 
     * @param transactionLog TransactionLog
     */
    void backup(TransactionLog transactionLog);

    /**
     * 恢复日志
     * 
     * @param databases List<Database>
     */
    void redo(DatabaseManager databaseManager);

    /**
     * 对日志进行重写
     * 
     * @param databases List<Database>
     */
    void reWrite(DatabaseManager databaseManager);
}
