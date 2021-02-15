package com.github.bdqfork.server;

import com.github.bdqfork.server.config.Configuration;

/**
 * 启动入口
 *
 * @author bdq
 * @since 2020/09/21
 */
public class Application {
    public static void main(String[] args) {
        String configPath = Configuration.DEFAULT_CONFIG_FILE_PATH;
        if (args.length > 0) {
            configPath = args[0];
        }
        JRedisServer jRedisServer = new JRedisServer(configPath);
        jRedisServer.listen();
        Runtime.getRuntime().addShutdownHook(new Thread(jRedisServer::close));
    }
}
