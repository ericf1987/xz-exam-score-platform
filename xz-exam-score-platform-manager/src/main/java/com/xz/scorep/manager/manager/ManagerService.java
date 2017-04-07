package com.xz.scorep.manager.manager;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xz.scorep.manager.manager.ExecutorAgent.EQUALS;

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
                .filter(EQUALS(host, port))
                .findFirst().orElse(null);
    }

    @NotNull
    public synchronized ExecutorAgent getOrCreateExecutorAgent(String host, int port) {
        return listExecutorAgents().stream()
                .filter(EQUALS(host, port))
                .findFirst().orElse(createExecutorAgent(host, port));
    }

    private ExecutorAgent createExecutorAgent(String host, int port) {
        ExecutorAgent executorAgent = new ExecutorAgent(host, port);
        executorAgents.add(executorAgent);
        return executorAgent;
    }

    public synchronized void updateExecutorAgent(
            String host, int port, long dataSize, List<ProjectStatus> activeProjects) {

        ExecutorAgent executorAgent = getExecutorAgent(host, port);

        if (executorAgent == null) {
            executorAgent = new ExecutorAgent(host, port);
            executorAgents.add(executorAgent);
        }

        executorAgent.setDataSize(dataSize);
        executorAgent.setActiveProjects(activeProjects);
    }

    public ExecutorAgent assignProject(String projectId) {
        return null;
    }
}
