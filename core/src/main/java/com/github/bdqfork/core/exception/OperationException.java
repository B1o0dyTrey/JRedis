package com.github.bdqfork.core.exception;

/**
 * @author bdq
 * @since 2020/09/22
 */
public class OperationException extends TransactionException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public OperationException(Throwable cause) {
        super(cause);
    }
}
