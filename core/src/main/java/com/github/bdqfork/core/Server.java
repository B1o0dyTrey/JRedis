package com.github.bdqfork.core;


import com.github.bdqfork.core.config.Configuration;
import com.github.bdqfork.core.handler.CommandInboundHandler;
import com.github.bdqfork.core.handler.codec.RESPDecoder;
import com.github.bdqfork.core.handler.codec.StringToByteEncoder;
import com.github.bdqfork.core.util.FileUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 服务端，接收用户请求
 *
 * @author bdq
 * @since 2020/9/20
 */
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final String host;
    private final Integer port;
    private Configuration configuration;
    private List<Database> databases;
    private EventLoopGroup boss;
    private EventLoopGroup worker;

    public Server(String host, Integer port) throws IOException {
        this(host, port, Configuration.DEFAULT_CONFIG_FILE_PATH);
    }

    public Server(String host, Integer port, String path) throws IOException {
        this.host = host;
        this.port = port;
        // todo: 从指定文件加载配置文件，初始化数据库、事务以及持久化管理
        loadConfiguration(path);
        initializeDatabases();
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
                        ch.pipeline().addLast(new StringToByteEncoder())
                                .addLast(new RESPDecoder())
                                .addLast(new CommandInboundHandler());
                    }
                });

        try {
            bootstrap.bind(host, port).sync();
        } catch (InterruptedException e) {
            log.warn(e.getMessage(), e);
            destroy();
        }
    }

    /**
     * 停止jredis服务
     */
    public void stop() {
        destroy();
    }

    private void destroy() {
        try {
            boss.shutdownGracefully().sync();
            worker.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 加载配置文件
     *
     * @throws IOException 配置文件不存在时抛出
     */
    private void loadConfiguration(String profilePath) throws IOException {
        Properties properties = FileUtils.loadPropertiesFile(profilePath);
        this.configuration = new Configuration();

        Integer databaseNumber = Integer.valueOf(properties.getProperty(
                "databaseNumber",Configuration.DEFAULT_CONFIG_DATABASES_NUMBER));
        configuration.setDatabaseNumber(databaseNumber);

        String serializer = properties.getProperty("serializer", Configuration.DEFAULT_CONFIG_SERIALIZER);
        configuration.setSerializer(serializer);

        String backupStrategy = properties.getProperty("backupStrategy", Configuration.DEFAULT_CONFIG_BACKUP_STRATEGY);
        configuration.setBackupStrategy(backupStrategy);

        String username = properties.getProperty("username");
        configuration.setUsername(username);

        String password = properties.getProperty("password");
        configuration.setPassword(password);
    }

    /**
     * 初始化所有数据库
     */
    private void initializeDatabases() {
        this.databases = new ArrayList<>();
        for (int i = 1; i <= configuration.getDatabaseNumber(); i++) {
            databases.add(new Database());
        }
        //todo 调用redo方法
    }
}
