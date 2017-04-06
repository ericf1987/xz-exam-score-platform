package com.xz.scorep.manager.manager;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示正在执行的服务实例
 *
 * @author yidin
 */
public class ExecutorAgent {

    public static int HEARTBEAT_TIMEOUT = 10000;

    private String host;

    private int port;

    private long registerTime;

    private long lastHeartBeat;

    private List<ProjectStatus> activeProjects = new ArrayList<>();

    public ExecutorAgent(String host, int port) {
        this.host = host;
        this.port = port;

        registerTime = lastHeartBeat = System.currentTimeMillis();
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

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getLastHeartBeat() {
        return lastHeartBeat;
    }

    public void setLastHeartBeat(long lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    public List<ProjectStatus> getActiveProjects() {
        return activeProjects;
    }

    public void setActiveProjects(List<ProjectStatus> activeProjects) {
        this.activeProjects = activeProjects;
    }

    //////////////////////////////////////////////////////////////

    public ExecutorAgentStatus getStatus() {
        return System.currentTimeMillis() - lastHeartBeat > HEARTBEAT_TIMEOUT ?
                ExecutorAgentStatus.Disconnected : ExecutorAgentStatus.Alive;
    }
}
