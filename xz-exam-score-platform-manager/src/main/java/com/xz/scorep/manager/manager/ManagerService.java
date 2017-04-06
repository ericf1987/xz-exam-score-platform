package com.xz.scorep.manager.manager;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ManagerService {

    private List<ExecutorAgent> executorAgents = new ArrayList<>();

    {
        ExecutorAgent a1 = new ExecutorAgent("10.10.22.154", 8081);
        a1.getActiveProjects().addAll(Arrays.asList(
                new ProjectStatus("project1", "Aggregating"),
                new ProjectStatus("project2", "Archiving")
        ));

        executorAgents.addAll(Arrays.asList(
                a1,
                new ExecutorAgent("10.10.22.155", 8081),
                new ExecutorAgent("10.10.22.156", 8081),
                new ExecutorAgent("10.10.22.157", 8081)
        ));
    }

    public synchronized List<ExecutorAgent> listExecutorAgents() {
        return executorAgents;
    }

    public synchronized ExecutorAgent getExecutorAgent(String host, int port) {
        return listExecutorAgents().stream()
                .filter(a -> a.getHost().equals(host) && a.getPort() == port)
                .findFirst().orElse(null);
    }

    public synchronized void updateExecutorAgent(String host, int port, List<ProjectStatus> activeProjects) {
        ExecutorAgent executorAgent = getExecutorAgent(host, port);
        if (executorAgent == null) {
            executorAgent = new ExecutorAgent(host, port);
            executorAgents.add(executorAgent);
        }
        executorAgent.setActiveProjects(activeProjects);
    }
}
