package com.github.bdqfork.client.netty;

import com.github.bdqfork.core.operation.OperationContext;
import com.github.bdqfork.core.MessageDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

/**
 * @author Trey
 * @since 2020/10/31
 */

public class NettyChannel {
    private static final Logger log = LoggerFactory.getLogger(NettyChannel.class);
    private static final int MAX_CAPCITY = 1024 * 1024;
    protected String host;
    protected Integer port;
    private EventLoopGroup group;
    private Channel channel;
    private BlockingQueue<OperationContext> queue;

    public NettyChannel(String host, Integer port,BlockingQueue<OperationContext> queue) {
        this.host = host;
        this.port = port;
        this.queue = queue;
    }


    public void open() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new MessageDecoder(MAX_CAPCITY))
                                .addLast(new StringEncoder(StandardCharsets.ISO_8859_1))
                                .addLast(new CommandHandler(queue));
                        if (channel != null && channel.isActive()) {
                            channel.closeFuture().sync();
                        }
                        channel = ch;
                    }
                });
        try {
            bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            destroy();
        }
    }

    public void send(Object data) {
        if (!channel.isActive()) {
            open();
        }
        channel.writeAndFlush(data);
    }

    public void close() {
        destroy();
    }

    private void destroy() {
        try {
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
