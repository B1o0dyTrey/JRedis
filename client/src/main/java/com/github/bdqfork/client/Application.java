package com.github.bdqfork.client;

import com.github.bdqfork.client.gui.CommandLineClient;

/**
 * 主启动类
 *
 * @author Trey
 * @since 2020/10/31
 */
public class Application {

    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 7000;
        if (args.length > 1) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        CommandLineClient commandLineClient = new CommandLineClient(host, port);
        commandLineClient.run();
    }
}
