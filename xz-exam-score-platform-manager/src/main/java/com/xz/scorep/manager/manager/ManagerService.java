package com.xz.scorep.manager.manager;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ManagerService {

    public List<ExecutorAgent> listExecutorAgents() {
        ExecutorAgent a1 = new ExecutorAgent("10.10.22.154", 8081);
        a1.getAggregatingProjects().addAll(Arrays.asList("1", "2", "3"));

        return Arrays.asList(
                a1,
                new ExecutorAgent("10.10.22.155", 8081),
                new ExecutorAgent("10.10.22.156", 8081),
                new ExecutorAgent("10.10.22.157", 8081)
        );
    }

    public ExecutorAgent getExecutorAgent(String host, int port) {
        return listExecutorAgents().stream()
                .filter(a -> a.getHost().equals(host) && a.getPort() == port)
                .findFirst().orElse(null);
    }
}
