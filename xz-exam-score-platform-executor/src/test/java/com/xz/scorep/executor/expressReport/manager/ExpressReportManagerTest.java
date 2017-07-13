package com.xz.scorep.executor.expressReport.manager;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/12.
 */
public class ExpressReportManagerTest extends BaseTest {

    @Autowired
    ExpressReportManager expressReportManager;

    @Test
    public void testStartTask() throws Exception {
        expressReportManager.startTask("430000-1527d8e87b5a48ed9ba7f9211c449035", "001");
    }
}