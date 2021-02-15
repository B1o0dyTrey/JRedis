package com.github.bdqfork.server.ops;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.bdqfork.core.exception.IllegalCommandException;
import com.github.bdqfork.core.exception.JRedisException;
import com.github.bdqfork.core.operation.KeyOperation;
import com.github.bdqfork.core.operation.Operation;
import com.github.bdqfork.core.operation.ValueOperation;
import com.github.bdqfork.core.protocol.LiteralWrapper;
import com.github.bdqfork.core.util.ReflectUtils;
import com.github.bdqfork.server.transaction.TransactionManager;

/**
 * @author bdq
 * @since 2020/11/11
 */
public class GenericServerOperation extends AbstractServerOperation {

    private final Map<String, Class<?>> operations = new HashMap<>();
    private final Map<String, Operation> operationInstances = new HashMap<>();

    private ServerValueOperation serverValueOperation;
    private ServerKeyOperation serverKeyOperation;

    public GenericServerOperation(int databaseId, TransactionManager transactionManager) {
        initKeyOperation(databaseId, transactionManager);
        initValueOperation(databaseId, transactionManager);
    }

    private void initKeyOperation(int databaseId, TransactionManager transactionManager) {
        serverKeyOperation = new ServerKeyOperation();
        serverKeyOperation.setDatabaseId(databaseId);
        serverKeyOperation.setTransactionManager(transactionManager);
        Arrays.stream(KeyOperation.class.getMethods()).map(Method::getName).distinct().forEach(name -> {
            operations.put(name, KeyOperation.class);
            operationInstances.put(name, serverKeyOperation);
        });
    }

    private void initValueOperation(int databaseId, TransactionManager transactionManager) {
        serverValueOperation = new ServerValueOperation();
        serverValueOperation.setDatabaseId(databaseId);
        serverValueOperation.setTransactionManager(transactionManager);
        Arrays.stream(ValueOperation.class.getMethods()).map(Method::getName).distinct().forEach(name -> {
            operations.put(name, ValueOperation.class);
            operationInstances.put(name, serverValueOperation);
        });
    }

    public LiteralWrapper<?> execute(String cmd, Object... args) throws JRedisException {

        if (!operations.containsKey(cmd)) {
            throw new IllegalCommandException("Illegal command");
        }

        Class<?> operationClass = operations.get(cmd);
        Object[] methodArgs = getMethodArgs(cmd, args);
        Class<?>[] parameterTypes = getParameterTypes(cmd, methodArgs);
        Operation operation = operationInstances.get(cmd);

        try {
            Method method = ReflectUtils.getMethod(operationClass, cmd, parameterTypes);
            Object result = method.invoke(operation, methodArgs);
            return encodeResult(result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new JRedisException(e);
        }
    }

    private Object[] getMethodArgs(String cmd, Object[] args) {
        if ("set".equals(cmd)) {
            if (args.length == 3) {
                args = Arrays.copyOf(args, args.length + 1);
                args[3] = TimeUnit.MILLISECONDS;
            }
        }
        return args;
    }

    private Class<?>[] getParameterTypes(String cmd, Object[] args) {
        if ("get".equals(cmd) || "ttl".equals(cmd) || "ttlAt".equals(cmd) || "del".equals(cmd)) {
            return new Class[] { String.class };
        }

        if ("set".equals(cmd) || "setnx".equals(cmd) || "setxx".equals(cmd)) {
            if (args.length == 4) {
                return new Class[] { String.class, Object.class, long.class, TimeUnit.class };
            }
            return new Class[] { String.class, Object.class };
        }
        if ("setex".equals(cmd) || "setpx".equals(cmd)) {
            return new Class[] { String.class, Object.class, long.class };
        }
        if ("expire".equals(cmd) || "expireAt".equals(cmd)) {
            return new Class[]{String.class, long.class};
        }
        throw new IllegalCommandException(String.format("Illegal command %s", cmd));
    }

    protected LiteralWrapper<?> encodeResult(Object result) {
        if (result instanceof String) {
            return LiteralWrapper.singleWrapper((String) result);
        }
        if (result instanceof Long) {
            return LiteralWrapper.integerWrapper((Number) result);
        }
        if (result instanceof Boolean) {
            return LiteralWrapper.integerWrapper((Boolean) result ? 1L : 0L);
        }
        if (result instanceof byte[]) {
            return LiteralWrapper.bulkWrapper((byte[]) result);
        }
        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<LiteralWrapper<?>> items = (List<LiteralWrapper<?>>) result;
            items = items.stream().map(this::encodeResult).collect(Collectors.toList());
            return LiteralWrapper.multiWrapper(items);
        }
        return LiteralWrapper.bulkWrapper();
    }

}
