package com.xz.scorep.executor.agent;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/4/7
 *
 * @author yidin
 */
public class AgentDaemonServiceTest extends BaseTest {

    @Autowired
    private AgentDaemonService agentDaemonService;

    @Test
    public void getDatabaseSize() throws Exception {
        System.out.println("database size: " + agentDaemonService.getDatabaseSize());
    }

}