package com.github.bdqfork.client.ops;

import com.github.bdqfork.client.netty.NettyChannel;
import com.github.bdqfork.core.CommandFuture;
import com.github.bdqfork.core.exception.SerializeException;
import com.github.bdqfork.core.exception.JRedisException;
import com.github.bdqfork.core.operation.OperationContext;
import com.github.bdqfork.core.protocol.LiteralWrapper;
import com.github.bdqfork.core.protocol.Type;
import com.github.bdqfork.core.serializtion.JdkSerializer;
import com.github.bdqfork.core.serializtion.Serializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author bdq
 * @since 2020/11/10
 */
public class OperationHandler implements InvocationHandler {
    private int databaseId;
    private NettyChannel nettyChannel;
    private BlockingQueue<OperationContext> queue;
    private Serializer serializer;

    public OperationHandler(int databaseId, NettyChannel nettyChannel, BlockingQueue<OperationContext> queue) {
        this(databaseId, nettyChannel, queue, new JdkSerializer());
    }

    public OperationHandler(int databaseId, NettyChannel nettyChannel, BlockingQueue<OperationContext> queue, Serializer serializer) {
        this.databaseId = databaseId;
        this.nettyChannel = nettyChannel;
        this.queue = queue;
        this.serializer = serializer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        String cmd = method.getName();
        Object[] cmdArgs = getCmdArgs(cmd, args);
        OperationContext operationContext = new OperationContext(databaseId, cmd, serialize(cmd, cmdArgs));

        CommandFuture commandFuture = new CommandFuture();
        operationContext.setResultFuture(commandFuture);

        try {
            queue.put(operationContext);
        } catch (InterruptedException e) {
            throw new JRedisException(e);
        }

        String msg = encode(operationContext);

        nettyChannel.send(msg);

        try {
            LiteralWrapper<?> literalWrapper = (LiteralWrapper<?>) commandFuture.get();
            if (literalWrapper.isTypeOf(Type.BULK) && literalWrapper.getData() != null) {
                return serializer.deserialize((byte[]) literalWrapper.getData(), Object.class);
            }
            Object result = literalWrapper.getData();
            return parseResult(cmd,result);
        } catch (InterruptedException | ExecutionException | SerializeException e) {
            throw new JRedisException(e);
        }
    }

    private Object parseResult(String cmd, Object result) {
        if ("setnx".equals(cmd) || "setxx".equals(cmd)) {
            result = (Long) result == 1;
        }
        return result;
    }

    private Object[] getCmdArgs(String cmd, Object[] args) {
        if ("set".equals(cmd) && args.length == 4) {
            if (args[3] instanceof TimeUnit) {
                Long expire = (Long) args[2];
                TimeUnit timeUnit = (TimeUnit) args[3];
                if (timeUnit == TimeUnit.MICROSECONDS) {
                    expire /= 1000;
                }
                if (timeUnit == TimeUnit.SECONDS) {
                    expire *= 1000;
                }
                if (timeUnit == TimeUnit.MINUTES) {
                    expire *= 1000 * 60;
                }
                if (timeUnit == TimeUnit.HOURS) {
                    expire *= 1000 * 60 * 60;
                }
                if (timeUnit == TimeUnit.DAYS) {
                    expire *= 1000 * 60 * 60 * 24;
                }
                args[2] = expire;
            }
            return Arrays.stream(args).limit(args.length - 1).toArray();
        }
        return args;
    }

    private Object[] serialize(String method, Object[] args) throws SerializeException {
        if ("set".equals(method) || "setnx".equals(method) || "setpx".equals(method)
            || "setxx".equals(method) || "setex".equals(method)) {
            args[1] = serializer.serialize(args[1]);
        }
        return args;
    }

    private String encode(OperationContext operationContext) {
        LiteralWrapper<LiteralWrapper<?>> literalWrapper = encodeArgs(operationContext.getArgs());

        @SuppressWarnings("unchecked")
        List<LiteralWrapper<?>> literalWrappers = (List<LiteralWrapper<?>>) literalWrapper.getData();

        LiteralWrapper<String> cmdLiteralWrapper = LiteralWrapper.singleWrapper();
        cmdLiteralWrapper.setData(operationContext.getCmd());
        literalWrappers.add(0, cmdLiteralWrapper);

        return literalWrapper.encode();
    }

    private LiteralWrapper<LiteralWrapper<?>> encodeArgs(Object[] args) {
        List<LiteralWrapper<?>> literalWrappers = new ArrayList<>();

        for (Object arg : args) {
            if (arg instanceof String) {
                LiteralWrapper<String> singleWrapper = LiteralWrapper.singleWrapper();
                singleWrapper.setData(arg);
                literalWrappers.add(singleWrapper);
            }
            if (arg instanceof Number) {
                LiteralWrapper<Long> integerWrapper = LiteralWrapper.integerWrapper();
                integerWrapper.setData(((Number) arg).longValue());
                literalWrappers.add(integerWrapper);
            }
            if (arg instanceof byte[]) {
                LiteralWrapper<byte[]> bulkWrapper = LiteralWrapper.bulkWrapper();
                bulkWrapper.setData(arg);
                literalWrappers.add(bulkWrapper);
            }
            if (arg instanceof List) {
                List<?> items = (List<?>) arg;
                literalWrappers.add(encodeArgs(items.toArray()));
            }
        }
        LiteralWrapper<LiteralWrapper<?>> wrapper = LiteralWrapper.multiWrapper();
        wrapper.setData(literalWrappers);
        return wrapper;
    }

}
