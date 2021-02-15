package com.github.bdqfork.core.protocol;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author bdq
 * @since 2020/11/6
 */
public class LiteralWrapper<T> {
    private Type type;
    private Object data;

    public static LiteralWrapper<String> singleWrapper() {
        return new LiteralWrapper<>(Type.SINGLE);
    }

    public static LiteralWrapper<String> singleWrapper(String data) {
        LiteralWrapper<String> literalWrapper = singleWrapper();
        literalWrapper.setData(data);
        return literalWrapper;
    }

    public static LiteralWrapper<String> errorWrapper() {
        return new LiteralWrapper<>(Type.ERROR);
    }

    public static LiteralWrapper<String> errorWrapper(String data) {
        LiteralWrapper<String> literalWrapper = errorWrapper();
        literalWrapper.setData(data);
        return literalWrapper;
    }

    public static LiteralWrapper<Long> integerWrapper() {
        return new LiteralWrapper<>(Type.INTEGER);
    }

    public static LiteralWrapper<Long> integerWrapper(Number number) {
        LiteralWrapper<Long> literalWrapper = integerWrapper();
        literalWrapper.setData(number);
        return literalWrapper;
    }

    public static LiteralWrapper<byte[]> bulkWrapper() {
        return new LiteralWrapper<>(Type.BULK);
    }

    public static LiteralWrapper<byte[]> bulkWrapper(byte[] data) {
        LiteralWrapper<byte[]> literalWrapper = bulkWrapper();
        literalWrapper.setData(data);
        return literalWrapper;
    }

    public static LiteralWrapper<LiteralWrapper<?>> multiWrapper() {
        return new LiteralWrapper<>(Type.MULTI);
    }

    public static LiteralWrapper<LiteralWrapper<?>> multiWrapper(List<? extends LiteralWrapper<?>> data) {
        LiteralWrapper<LiteralWrapper<?>> literalWrapper = multiWrapper();
        literalWrapper.setData(data);
        return literalWrapper;
    }

    private LiteralWrapper(Type type) {
        this.type = type;
    }

    public boolean isTypeOf(Type type) {
        return this.type == type;
    }

    public Type getType() {
        return type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public T getData() {
        return (T) data;
    }

    public String encode() {
        StringBuilder builder = new StringBuilder();
        Stack<LiteralWrapper<?>> stack = new Stack<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            LiteralWrapper<?> literalWrapper = stack.pop();
            if (literalWrapper.isTypeOf(Type.SINGLE)) {
                String data = (String) literalWrapper.getData();
                builder.append('+').append(data).append("\r\n");
            }

            if (literalWrapper.isTypeOf(Type.ERROR)) {
                String data = (String) literalWrapper.getData();
                builder.append('-').append(data).append("\r\n");
            }

            if (literalWrapper.isTypeOf(Type.INTEGER)) {
                Long data = (Long) literalWrapper.getData();
                builder.append(':').append(data).append("\r\n");
            }

            if (literalWrapper.isTypeOf(Type.BULK)) {
                byte[] data = (byte[]) literalWrapper.getData();
                if (data == null) {
                    builder.append('$').append(-1).append("\r\n");
                } else {
                    String bulk = new String(data, StandardCharsets.ISO_8859_1);
                    builder.append('$').append(bulk.length()).append("\r\n");
                    builder.append(bulk).append("\r\n");
                }
            }

            if (literalWrapper.isTypeOf(Type.MULTI)) {
                @SuppressWarnings("unchecked")
                List<LiteralWrapper<?>> data = (List<LiteralWrapper<?>>) literalWrapper.getData();
                if (data == null) {
                    builder.append('*').append(-1).append("\r\n");
                } else {
                    builder.append('*').append(data.size()).append("\r\n");
                    for (int i = data.size() - 1; i >= 0; i--) {
                        stack.push(data.get(i));
                    }
                }
            }
        }
        return builder.toString();
    }

    public String toPlain() {
        StringBuilder builder = new StringBuilder();
        Stack<LiteralWrapper<?>> stack = new Stack<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            LiteralWrapper<?> literalWrapper = stack.pop();
            if (literalWrapper.isTypeOf(Type.SINGLE)) {
                String data = (String) literalWrapper.getData();
                builder.insert(0, "\r\n").insert(0, data);
            }

            if (literalWrapper.isTypeOf(Type.ERROR)) {
                String data = (String) literalWrapper.getData();
                builder.insert(0, "\r\n").insert(0, data);
            }

            if (literalWrapper.isTypeOf(Type.INTEGER)) {
                Long data = (Long) literalWrapper.getData();
                builder.insert(0, "\r\n").insert(0, data);
            }

            if (literalWrapper.isTypeOf(Type.BULK)) {
                byte[] data = (byte[]) literalWrapper.getData();
                if (data == null) {
                    builder.insert(0, "\r\n").insert(0, "nil");
                } else {
                    builder.insert(0, "\r\n").insert(0, new String(data, StandardCharsets.ISO_8859_1));
                }
            }

            if (literalWrapper.isTypeOf(Type.MULTI)) {
                @SuppressWarnings("unchecked")
                List<LiteralWrapper<?>> data = (List<LiteralWrapper<?>>) literalWrapper.getData();
                if (data == null) {
                    builder.insert(0, "\r\n").insert(0, "nil");
                } else {
                    data.forEach(stack::push);
                }
            }
        }
        return builder.toString();
    }
}
