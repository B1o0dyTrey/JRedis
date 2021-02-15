package com.github.bdqfork.core;

/**
 * @author bdq
 * @since 2020/11/9
 */
public class Session {
    private String clientAddr;

    private Integer clientPort;

    private Integer databaseId;

    public Session(String clientAddr, Integer clientPort, Integer databaseId) {
        this.clientAddr = clientAddr;
        this.clientPort = clientPort;
        this.databaseId = databaseId;
    }

    public String getClientAddr() {
        return clientAddr;
    }

    public void setClientAddr(String clientAddr) {
        this.clientAddr = clientAddr;
    }

    public Integer getClientPort() {
        return clientPort;
    }

    public void setClientPort(Integer clientPort) {
        this.clientPort = clientPort;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
    }

}
