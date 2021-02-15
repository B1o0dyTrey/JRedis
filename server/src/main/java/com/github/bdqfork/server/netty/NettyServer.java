package com.github.bdqfork.server.netty;


import com.github.bdqfork.core.MessageDecoder;
import com.github.bdqfork.server.Dispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 服务端，接收用户请求
 *
 * @author bdq
 * @since 2020/9/20
 */
public class NettyServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
    private static final int MAX_CAPCITY = 1024 * 1024;
    private final String host;
    private final Integer port;
    private EventLoopGroup boss;
    private EventLoopGroup worker;
    private Dispatcher dispatcher;

    public NettyServer(String host, Integer port, Dispatcher dispatcher) {
        this.host = host;
        this.port = port;
        this.dispatcher = dispatcher;
    }

    /**
     * 启动服务端
     */
    public void start() {
        boss = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new MessageDecoder(MAX_CAPCITY))
                                .addLast(new StringEncoder(StandardCharsets.ISO_8859_1))
                                .addLast(new CommandHandler(dispatcher));
                    }
                });

        try {
            bootstrap.bind(host, port).sync();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            destroy();
        }
    }

    public void stop() {
        destroy();
    }

    private void destroy() {
        try {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
