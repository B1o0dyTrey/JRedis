package com.github.bdqfork.server.ops;

import com.github.bdqfork.server.transaction.OperationType;

/**
 * @author bdq
 * @since 2020/11/6
 */
public abstract class UpdateCommand implements Command {

    @Override
    public OperationType getOperationType() {
        return OperationType.UPDATE;
    }

}
