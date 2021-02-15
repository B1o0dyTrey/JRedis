package com.github.bdqfork.core.protocol;

import com.github.bdqfork.core.exception.JRedisException;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @author bdq
 * @since 2020/11/5
 */
public class StateMachine {
    private Stack<State> stack;

    public StateMachine() {
        this.stack = new Stack<>();
    }

    public LiteralWrapper<?> decode(ByteBuf byteBuf) {
        if (stack.isEmpty()) {
            stack.push(new State());
        }
        LiteralWrapper<?> latestLiteralWrapper = null;
        while (!stack.isEmpty()) {
            State state = stack.peek();
            if (state.type == null) {
                state.type = parseType(byteBuf);
            }
            if (state.wrapper == null) {
                state.wrapper = getWrapper(state.type);
            }

            if (state.type == Type.SINGLE) {
                state.wrapper.setData(readLine(byteBuf));
                stack.pop();
                latestLiteralWrapper = state.wrapper;
            }
            if (state.type == Type.ERROR) {
                state.wrapper.setData(readLine(byteBuf));
                stack.pop();
                latestLiteralWrapper = state.wrapper;
            }
            if (state.type == Type.INTEGER) {
                state.wrapper.setData(readLong(byteBuf));
                stack.pop();
                latestLiteralWrapper = state.wrapper;
            }
            if (state.type == Type.BYTES) {
                state.wrapper.setData(readBytes(byteBuf));
                stack.pop();
                latestLiteralWrapper = state.wrapper;
            }
            if (state.type == Type.BULK) {
                state.count = readLong(byteBuf);
                if (state.count == -1) {
                    stack.pop();
                    latestLiteralWrapper = state.wrapper;
                } else {
                    state.type = Type.BYTES;
                    break;
                }
            }
            if (state.type == Type.MULTI) {
                if (state.count == -1) {
                    stack.pop();
                    latestLiteralWrapper = state.wrapper;
                } else if (state.count == Long.MIN_VALUE) {
                    state.count = readLong(byteBuf);
                    state.wrapper = LiteralWrapper.multiWrapper();
                    if (state.count >= 0) {
                        List<LiteralWrapper<?>> literalWrappers = new ArrayList<>((int) state.count);
                        state.wrapper.setData(literalWrappers);
                    }
                } else if (state.count == 0) {
                    stack.pop();
                    if (latestLiteralWrapper != null) {
                        @SuppressWarnings("unchecked")
                        List<LiteralWrapper<?>> literalWrappers = (List<LiteralWrapper<?>>) state.wrapper.getData();
                        literalWrappers.add(latestLiteralWrapper);
                    }
                    latestLiteralWrapper = state.wrapper;
                } else {
                    if (latestLiteralWrapper != null) {
                        @SuppressWarnings("unchecked")
                        List<LiteralWrapper<?>> literalWrappers = (List<LiteralWrapper<?>>) state.wrapper.getData();
                        literalWrappers.add(latestLiteralWrapper);
                    }
                    stack.push(new State());
                    state.count--;
                    break;
                }
            }

            if (stack.isEmpty()) {
                return state.wrapper;
            }
        }
        return null;
    }

    private Object readBytes(ByteBuf byteBuf) {
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);
        return data;
    }

    private LiteralWrapper<?> getWrapper(Type type) {
        switch (type) {
            case SINGLE:
                return LiteralWrapper.singleWrapper();
            case ERROR:
                return LiteralWrapper.errorWrapper();
            case INTEGER:
                return LiteralWrapper.integerWrapper();
            case BULK:
                return LiteralWrapper.bulkWrapper();
            case MULTI:
                return LiteralWrapper.multiWrapper();
            default:
                throw new JRedisException("Not support type");
        }
    }

    private Long readLong(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        String num = new String(bytes);
        return Long.parseLong(num);
    }

    private String readLine(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return new String(bytes);
    }

    private Type parseType(ByteBuf byteBuf) {
        switch (byteBuf.readByte()) {
            case '+':
                return Type.SINGLE;
            case '-':
                return Type.ERROR;
            case ':':
                return Type.INTEGER;
            case '$':
                return Type.BULK;
            case '*':
                return Type.MULTI;
            default:
                throw new JRedisException("Invalid first byte");
        }
    }

    private static class State {
        Type type;
        long count = Long.MIN_VALUE;
        LiteralWrapper<?> wrapper;
    }

}
