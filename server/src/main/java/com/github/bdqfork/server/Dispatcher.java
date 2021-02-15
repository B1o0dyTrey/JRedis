package com.github.bdqfork.server;

import com.github.bdqfork.core.operation.OperationContext;
import com.github.bdqfork.core.CommandFuture;
import com.github.bdqfork.core.exception.JRedisException;
import com.github.bdqfork.core.protocol.LiteralWrapper;
import com.github.bdqfork.server.ops.GenericServerOperation;
import com.github.bdqfork.server.transaction.TransactionManager;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author bdq
 * @since 2020/11/6
 */
public class Dispatcher {
    private CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean destroyed;
    private BlockingQueue<OperationContext> queue;
    private TransactionManager transactionManager;
    private Map<Integer, GenericServerOperation> serverOperations = new ConcurrentHashMap<>();

    public Dispatcher(TransactionManager transactionManager, BlockingQueue<OperationContext> queue) {
        this.transactionManager = transactionManager;
        this.queue = queue;
    }

    public CommandFuture dispatch(OperationContext operationContext) {
        CommandFuture commandFuture = new CommandFuture();
        operationContext.setResultFuture(commandFuture);
        try {
            queue.put(operationContext);
        } catch (InterruptedException e) {
            throw new JRedisException(e);
        }
        return operationContext.getResultFuture();
    }

    public void accept() {
        CountDownLatch startedLatch = new CountDownLatch(1);
        Thread handler = new Thread(new Runnable() {
            @Override
            public void run() {
                startedLatch.countDown();
                while (!destroyed) {
                    OperationContext operationContext;
                    try {
                        operationContext = queue.take();
                    } catch (InterruptedException e) {
                        throw new JRedisException(e);
                    }
                    handle(operationContext);
                }
                latch.countDown();
            }
        });
        handler.setName("Main-Handler");
        handler.start();
        try {
            startedLatch.await();
        } catch (InterruptedException e) {
            throw new JRedisException(e);
        }
    }

    private void handle(OperationContext operationContext) {
        int databaseId = operationContext.getDatabaseId();

        GenericServerOperation genericServerOperation = getGenericServerOperation(databaseId);

        String cmd = operationContext.getCmd();
        Object[] args = operationContext.getArgs();

        CommandFuture commandFuture = operationContext.getResultFuture();
        try {
            LiteralWrapper<?> literalWrapper = genericServerOperation.execute(cmd, args);
            commandFuture.complete(literalWrapper);
        } catch (JRedisException e) {
            commandFuture.completeExceptionally(e);
        }

    }

    private GenericServerOperation getGenericServerOperation(int databaseId) {
        GenericServerOperation genericServerOperation;
        if (serverOperations.containsKey(databaseId)) {
            genericServerOperation = serverOperations.get(databaseId);
        } else {
            genericServerOperation = new GenericServerOperation(databaseId, transactionManager);
            serverOperations.put(databaseId, genericServerOperation);
        }
        return genericServerOperation;
    }

    public void stop() {
        destroyed = true;
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new JRedisException(e);
        }
    }

}
