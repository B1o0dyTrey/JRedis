package com.github.bdqfork.server.transaction;

import java.io.Serializable;
import java.util.List;

/**
 * @author bdq
 * @since 2020/09/22
 */
public class TransactionLog implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Long transactionId;
    private List<RedoLog> redoLogs;

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public List<RedoLog> getRedoLogs() {
        return redoLogs;
    }

    public void setRedoLogs(List<RedoLog> redoLogs) {
        this.redoLogs = redoLogs;
    }
}
