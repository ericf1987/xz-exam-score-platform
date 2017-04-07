package com.xz.scorep.manager.manager;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    private Map<String, ExecutorAgent> projectMap = new HashMap<>();

    public void setProjects(ExecutorAgent executorAgent, String... projectIds) {
        setProjects(executorAgent, Arrays.asList(projectIds));
    }

    public void setProjects(ExecutorAgent executorAgent, List<String> projectIds) {
        projectIds.forEach(projectId -> projectMap.put(projectId, executorAgent));
    }

    public ExecutorAgent getProjectExecutorAgent(String projectId) {
        return projectMap.get(projectId);
    }
}
