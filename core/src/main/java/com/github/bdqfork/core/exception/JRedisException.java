package com.github.bdqfork.core.exception;

/**
 * @author bdq
 * @since 2020/11/5
 */
public class JRedisException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public JRedisException(String message) {
        super(message);
    }

    public JRedisException(Throwable cause) {
        super(cause);
    }
}
