package com.xz.scorep.executor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "manager")
public class ManagerConfig {

    // 心跳请求最小间隔，要保证两次心跳请求之间的时间间隔小于 MIN_INTERVAL
    public static final int MIN_INTERVAL = 5000;

    private String host;

    private int port;

    private int interval;

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    //////////////////////////////////////////////////////////////

    public String heartBeatUrl() {
        return "http://" + host + ":" + port + "/agent/heartbeat";
    }
}
