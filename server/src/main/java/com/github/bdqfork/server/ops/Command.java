package com.github.bdqfork.server.ops;

import com.github.bdqfork.server.database.DatabaseManager;
import com.github.bdqfork.server.transaction.OperationType;

/**
 * @author bdq
 * @since 2020/11/6
 */
public interface Command {
    String getKey();

    Object execute(DatabaseManager databaseManager, int databaseId);

    OperationType getOperationType();
}
