package com.github.bdqfork.server.transaction;

import com.github.bdqfork.core.exception.TransactionException;
import com.github.bdqfork.server.ops.Command;
import com.github.bdqfork.server.ops.DeleteCommand;
import com.github.bdqfork.server.ops.UpdateCommand;
import com.github.bdqfork.server.database.DatabaseManager;
import com.github.bdqfork.server.transaction.backup.BackupStrategy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 负责进行事务管理
 *
 * @author bdq
 * @since 2020/9/20
 */
public class TransactionManager {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    private final Map<Long, Transaction> transactionMap = new ConcurrentHashMap<>(256);
    private final BackupStrategy strategy;
    private final DatabaseManager databaseManager;

    public TransactionManager(BackupStrategy strategy, DatabaseManager databaseManager) {
        this.strategy = strategy;
        this.databaseManager = databaseManager;
        strategy.redo(databaseManager);
    }

    private static Long newId() {
        return ID_GENERATOR.getAndIncrement();
    }

    /**
     * 准备事务
     *
     * @param databaseId 数据库id
     * @param command    命令
     */
    public Long prepare(int databaseId, Command command) {
        Long transactionId = newId();
        Transaction transaction = new Transaction(transactionId, databaseId, command);
        transactionMap.put(transactionId, transaction);
        return transactionId;
    }

    /**
     * 提交事务
     *
     * @return Object 事务执行结果
     */
    public Object commit(Long transactionId) throws TransactionException {
        Transaction transaction = transactionMap.get(transactionId);
        int databaseId = transaction.getDatabaseId();
        Command command = transaction.getCommand();
        return doCommit(transaction, databaseId, command);
    }

    private Object doCommit(Transaction transaction, int databaseId, Command command) {

        if (!(command instanceof UpdateCommand) && !(command instanceof DeleteCommand)) {
            return command.execute(databaseManager, databaseId);
        }

        UndoLog undoLog = createUndoLog(databaseId, command.getKey());
        transaction.addUndoLog(undoLog);

        Object result = command.execute(databaseManager, databaseId);

        RedoLog redoLog = createRedoLog(databaseId, command.getKey(), command.getOperationType());
        transaction.addRedoLog(redoLog);
        return result;
    }

    /**
     * 回滚事务
     */
    public void rollback(Long transactionId) {

        Transaction transaction = transactionMap.get(transactionId);
        List<UndoLog> undoLogs = transaction.getUndoLogs();

        for (UndoLog undoLog : undoLogs) {

            int databaseId = undoLog.getDatabaseId();
            databaseManager.saveOrUpdate(databaseId, undoLog.getKey(), undoLog.getValue(), undoLog.getExpireAt());

            RedoLog redoLog = createRedoLog(databaseId, undoLog.getKey(), OperationType.UPDATE);
            transaction.addRedoLog(redoLog);
        }
    }

    /**
     * 备份Log
     * 
     * @param transactionId
     */
    public void backup(Long transactionId) {
        Transaction transaction = transactionMap.get(transactionId);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setTransactionId(transaction.getTransactionId());
        transactionLog.setRedoLogs(transaction.getRedoLogs());
        strategy.backup(transactionLog);

        transactionMap.remove(transactionId);
    }

    private UndoLog createUndoLog(int databaseId, String key) {
        Object value = databaseManager.get(databaseId, key);
        Long expireAt = databaseManager.ttlAt(databaseId, key) == null ? -1 : databaseManager.ttlAt(databaseId, key);

        UndoLog undoLog = new UndoLog();
        undoLog.setDatabaseId(databaseId);
        undoLog.setKey(key);
        undoLog.setValue(value);
        undoLog.setExpireAt(expireAt);

        return undoLog;
    }

    private RedoLog createRedoLog(int databaseId, String key, OperationType type) {
        Object value = databaseManager.get(databaseId, key);
        Long expireAt = databaseManager.ttlAt(databaseId, key) == null ? -1 : databaseManager.ttlAt(databaseId, key);

        RedoLog redoLog = new RedoLog();
        redoLog.setDatabaseId(databaseId);
        redoLog.setKey(key);
        redoLog.setValue(value);
        redoLog.setOperationType(type);
        redoLog.setExpireAt(expireAt);

        return redoLog;
    }
}
