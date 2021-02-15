package com.github.bdqfork.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bdq
 * @since 2020/11/6
 */
public class SessionHolder {
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>(256);

    public static Session getSession(String clientHost, Integer clientPort) {
        return sessions.get(clientHost + ":" + clientPort);
    }

    public static Session setSession(Session session) {
        return sessions.put(session.getClientAddr() + ":" + session.getClientPort(), session);
    }

}
