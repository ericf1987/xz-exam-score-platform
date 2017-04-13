package com.xz.scorep.manager.manager;

import com.alibaba.fastjson.JSON;
import com.hyd.simplecache.SimpleCache;
import com.xz.ajiaedu.common.http.HttpRequest;
import com.xz.ajiaedu.common.lang.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    private ManagerService managerService;

    @Autowired
    private SimpleCache simpleCache;

    public ExecutorAgent getProjectExecutorAgent(String projectId) {
        return simpleCache.get("agent:" + projectId, () -> {
            for (ExecutorAgent executorAgent : managerService.listExecutorAgents()) {
                if (projectExists(executorAgent, projectId)) {
                    return executorAgent;
                }
            }
            return null;
        }, 10);
    }

    private boolean projectExists(ExecutorAgent agent, String projectId) {
        String checkProjectUrl = "http://" + agent.getHost() + ":" + agent.getPort() + "/project/status";
        HttpRequest request = new HttpRequest(checkProjectUrl).setParameter("projectId", projectId);

        try {
            Result result = JSON.parseObject(request.request(), Result.class);
            return result.isSuccess() && result.getBoolean("projectImported", false);
        } catch (IOException e) {
            LOG.error("Error looking for project " + projectId, e);
            return false;
        }
    }
}
