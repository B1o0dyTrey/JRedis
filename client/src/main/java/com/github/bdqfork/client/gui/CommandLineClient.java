package com.github.bdqfork.client.gui;

import com.github.bdqfork.core.exception.IllegalCommandException;
import com.github.bdqfork.client.ops.JRedisClient;
import com.github.bdqfork.core.exception.JRedisException;
import com.github.bdqfork.core.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author bdq
 * @since 2020/11/10
 */
public class CommandLineClient {
    private static final String PREFIX_FORMAT = "%s:%d>";
    private static final String EXIT_CMD = "exit";
    private JRedisClient jRedisClient;
    private GenericClientOperation operation;
    private String host;
    private Integer port;

    public CommandLineClient(String hot, Integer port) {
        this.host = hot;
        this.port = port;
        this.jRedisClient = new JRedisClient(host, port, 0);
        this.operation = new GenericClientOperation();
    }

    public void run() {
        try {
            jRedisClient.connect();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(0);
        }
        operation.reset(jRedisClient);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.printf(PREFIX_FORMAT, host, port);
            String line = scanner.nextLine();
            if (EXIT_CMD.equals(line)) {
                break;
            }
            String[] lits = line.split(" ");
            if (lits.length == 0) {
                continue;
            }
            String cmd = getCmd(lits);
            Object[] args;
            try {
                args = getArgs(cmd, lits);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                continue;
            }

            Object result = null;
            try {
                result = operation.execute(cmd, args);
            } catch (IllegalCommandException e) {
                System.out.println("Error: " + e.getMessage());
                continue;
            } catch (JRedisException e) {
                System.out.println("Error: " + e.getMessage());
                System.exit(0);
            } catch (Throwable e) {
                System.out.println("Error: " + e.getMessage());
                continue;
            }

            if (result == null) {
                if ("get".equals(cmd)) {
                    System.out.println("nil");
                } else {
                    System.out.println("OK");
                }
            } else {
                System.out.println(result);
            }
        }
        scanner.close();
    }

    private Object[] getArgs(String cmd, String[] lits) {
        List<Object> args = Arrays.stream(lits).skip(1)
                .map(lit -> StringUtils.isNumeric(lit) ? Long.parseLong(lit) : lit).collect(Collectors.toList());
        // todo set其他方法实现参数获取
        if ("set".equals(cmd)) {
            if (args.size() == 3) {
                if (!(args.get(2) instanceof Long)) {
                    throw new JRedisException("Command is invalid");
                } else {
                    args.add(TimeUnit.MILLISECONDS);
                }
            }
        }
        if (cmd.startsWith("set") && !(args.get(1) instanceof Long)) {
            args.set(1, getValueString((String) args.get(1)));
        }
        return args.toArray();
    }

    private String getCmd(String[] lits) {
        return lits[0];
    }

    private String getValueString(String str) {
        if (!StringUtils.isNumeric(str)) {
            char[] chars = str.toCharArray();
            if (chars[0] != '\"' || chars[chars.length - 1] != '\"') {
                throw new IllegalArgumentException("The string must be wrapped in double quotation marks");
            }
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < chars.length - 1; i++) {
                builder.append(chars[i]);
            }
            return builder.toString();
        }
        return str;
    }
}
