package com.xz.scorep.executor.agent;

import com.xz.scorep.executor.config.ManagerConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class AgentDaemonService {

    private static final Logger LOG = LoggerFactory.getLogger(AgentDaemonService.class);

    private Thread daemonThread;

    @Autowired
    private ManagerConfig managerConfig;

    @PostConstruct
    private void initAgentDaemonService() {
        initThread();
    }

    private void initThread() {
        if (StringUtils.isBlank(managerConfig.getHost())) {
            return;
        }

        if (managerConfig.getPort() == 0) {
            return;
        }

        this.daemonThread = new Thread(this::run);
        this.daemonThread.setDaemon(true);
        this.daemonThread.start();
    }

    private void run() {
        while (true) {
            try {
                run0();
                Thread.sleep(managerConfig.getInterval());
            } catch (Exception e) {
                LOG.error("后台保持进程错误", e);
            }
        }
    }

    private void run0() {

    }
}
