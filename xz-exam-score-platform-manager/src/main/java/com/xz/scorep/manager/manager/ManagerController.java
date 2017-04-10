package com.xz.scorep.manager.manager;

import com.alibaba.fastjson.JSON;
import com.xz.ajiaedu.common.lang.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ManagerController {

    private static final Logger LOG = LoggerFactory.getLogger(ManagerController.class);

    @Autowired
    private ManagerService managerService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/agent/heartbeat")
    @ResponseBody
    public Result heartbeat(
            @RequestParam("host") String host,
            @RequestParam("port") int port,
            @RequestParam("dataSize") long dataSize,
            @RequestParam("status") String statusJson
    ) {
        try {
            List<ProjectStatus> activeProjects = JSON.parseArray(statusJson, ProjectStatus.class);
            activeProjects.removeIf(ps -> !ps.getStatus().equals("Ready"));
            managerService.updateExecutorAgent(host, port, dataSize, activeProjects);
            return Result.success();
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.toString());
        }
    }

    @PostMapping("/agent/projects")
    @ResponseBody
    public Result pushProjectList(
            @RequestParam("host") String host,
            @RequestParam("port") int port,
            @RequestParam("projects") String projectsJson
    ) {
        try {
            ExecutorAgent executorAgent = managerService.getOrCreateExecutorAgent(host, port);
            List<String> projectIds = JSON.parseArray(projectsJson, String.class);
            projectService.setProjects(executorAgent, projectIds);
            return Result.success();
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.toString());
        }
    }

    /**
     * 获取项目所属的服务器
     *
     * @param projectId  项目ID
     * @param autoAssign 当项目不存在时，是否自动指派一个。如果为 false，则当项目不存在时返回 exists=false
     *
     * @return exists=是否有返回结果；host=服务器地址；port=服务器端口
     */
    @PostMapping("/getProjectServer")
    @ResponseBody
    public Result getProjectServer(
            @RequestParam("projectId") String projectId,
            @RequestParam("autoAssign") String autoAssign
    ) {
        ExecutorAgent executorAgent = projectService.getProjectExecutorAgent(projectId);

        if (executorAgent == null && autoAssign.equalsIgnoreCase("true")) {
            executorAgent = managerService.assignProject(projectId);
            projectService.setProjects(executorAgent, projectId);
        }

        if (executorAgent != null) {
            return Result.success().set("exists", true)
                    .set("host", executorAgent.getHost()).set("port", executorAgent.getPort());
        } else {
            return Result.success().set("exists", false);
        }
    }
}
