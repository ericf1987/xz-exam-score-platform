package com.xz.scorep.manager.manager;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.xz.scorep.manager.manager.ExecutorAgent.EQUALS;

@Service
public class ManagerService {

    private List<ExecutorAgent> executorAgents = new ArrayList<>();

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
            String host, int port, long dataSize, List<ProjectStatus> projects) {

        ExecutorAgent executorAgent = getExecutorAgent(host, port);

        if (executorAgent == null) {
            executorAgent = new ExecutorAgent(host, port);
            executorAgents.add(executorAgent);
        }

        List<ProjectStatus> activeProjects = new ArrayList<>(projects);
        activeProjects.removeIf(p -> p.getStatus().equals("Ready"));

        executorAgent.setLastHeartBeat(System.currentTimeMillis());
        executorAgent.setDataSize(dataSize);
        executorAgent.setActiveProjects(activeProjects);
    }

    public synchronized ExecutorAgent assignProject(String projectId) {
        if (executorAgents.isEmpty()) {
            return null;
        }

        // 取状态为 Alive 且 dataSize 最小的元素
        return executorAgents.stream()
                .filter(a -> a.getStatus() == ExecutorAgentStatus.Alive)
                .sorted((a1, a2) -> Long.signum(a1.getDataSize() - a2.getDataSize()))
                .findFirst().orElse(null);
    }
}
