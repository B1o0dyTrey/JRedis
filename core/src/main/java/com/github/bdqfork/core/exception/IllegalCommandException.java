package com.github.bdqfork.core.exception;

/**
 * @author bdq
 * @since 2020/11/11
 */
public class IllegalCommandException extends JRedisException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public IllegalCommandException(String message) {
        super(message);
    }

    public IllegalCommandException(Throwable cause) {
        super(cause);
    }
}
