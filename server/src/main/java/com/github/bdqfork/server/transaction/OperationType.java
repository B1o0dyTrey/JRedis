package com.github.bdqfork.server.transaction;

/**
 * @author Trey
 * @since 2021/1/10
 */

public enum OperationType {
    UPDATE(0), DELETE(1), QUERY(2);

    private int value;

    private OperationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static OperationType getOperationType(int value) {
        if (value == 0) {
            return OperationType.UPDATE;
        }
        if (value == 1) {
            return OperationType.DELETE;
        }
        if (value == 2) {
            return OperationType.QUERY;
        }
        return null;
    }
}
