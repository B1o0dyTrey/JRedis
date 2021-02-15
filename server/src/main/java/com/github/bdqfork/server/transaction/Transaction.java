package com.github.bdqfork.server.transaction;

import com.github.bdqfork.server.ops.Command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author bdq
 * @since 2020/09/22
 */
public class Transaction {
    private final Long transactionId;
    private final Integer databaseId;
    private final Command command;
    private final List<RedoLog> redoLogs = new ArrayList<>();
    private final List<UndoLog> undoLogs = new LinkedList<>();

    public Transaction(Long transactionId, Integer databaseId, Command command) {
        this.transactionId = transactionId;
        this.databaseId = databaseId;
        this.command = command;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public Command getCommand() {
        return command;
    }

    public List<RedoLog> getRedoLogs() {
        return new ArrayList<>(redoLogs);
    }

    public List<UndoLog> getUndoLogs() {
        return new ArrayList<>(undoLogs);
    }

    public void addRedoLog(RedoLog redoLog) {
        this.redoLogs.add(redoLog);
    }

    public void addUndoLog(UndoLog undoLog) {
        this.undoLogs.add(undoLog);
    }

}
