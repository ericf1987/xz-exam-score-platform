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
    ManagerService managerService;

    @PostMapping("/agent/heartbeat")
    @ResponseBody
    public Result heartbeat(
            @RequestParam("host") String host,
            @RequestParam("port") int port,
            @RequestParam("status") String statusJson
    ) {
        try {
            List<ProjectStatus> activeProjects = JSON.parseArray(statusJson, ProjectStatus.class);
            managerService.updateExecutorAgent(host, port, activeProjects);
            return Result.success();
        } catch (Exception e) {
            LOG.error("", e);
            return Result.fail(e.toString());
        }
    }
}
