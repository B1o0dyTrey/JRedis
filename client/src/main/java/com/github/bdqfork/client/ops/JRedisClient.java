package com.github.bdqfork.client.ops;

import com.github.bdqfork.client.netty.NettyChannel;
import com.github.bdqfork.core.operation.KeyOperation;
import com.github.bdqfork.core.operation.OperationContext;
import com.github.bdqfork.core.operation.ValueOperation;
import com.github.bdqfork.core.proxy.javassist.Proxy;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author bdq
 * @since 2020/11/4
 */
public class JRedisClient {
    private final BlockingQueue<OperationContext> queue = new ArrayBlockingQueue<>(256);
    private final String host;
    private final Integer port;
    private int databaseId;
    private NettyChannel nettyChannel;

    public JRedisClient(String host, Integer port, int databaseId) {
        this.host = host;
        this.port = port;
        this.databaseId = databaseId;
    }

    public void connect() {
        nettyChannel = new NettyChannel(host, port, queue);
        nettyChannel.open();
    }

    public synchronized ValueOperation OpsForValue() {
        return (ValueOperation) Proxy.newProxyInstance(ValueOperation.class.getClassLoader(),
                new Class[]{ValueOperation.class}, new OperationHandler(databaseId, nettyChannel, queue));
    }

    public synchronized KeyOperation OpsForKey() {
        return (KeyOperation) Proxy.newProxyInstance(KeyOperation.class.getClassLoader(),
                new Class[]{KeyOperation.class}, new OperationHandler(databaseId, nettyChannel, queue));
    }

    public void close() {
        nettyChannel.close();
    }

    public void setDatabaseId(int databaseId) {
        this.databaseId = databaseId;
    }
}
