package com.github.bdqfork.core.operation;

import com.github.bdqfork.core.CommandFuture;

/**
 * @author bdq
 * @since 2020/11/9
 */
public class OperationContext {
    private int databaseId;
    private String cmd;
    private Object[] args;
    private CommandFuture resultFuture;

    public OperationContext(int databaseId, String cmd, Object[] args) {
        this.databaseId = databaseId;
        this.cmd = cmd;
        this.args = args;
    }

    public int getDatabaseId() {
        return databaseId;
    }

    public String getCmd() {
        return cmd;
    }

    public Object[] getArgs() {
        return args;
    }

    public CommandFuture getResultFuture() {
        return resultFuture;
    }

    public void setResultFuture(CommandFuture resultFuture) {
        this.resultFuture = resultFuture;
    }
}
