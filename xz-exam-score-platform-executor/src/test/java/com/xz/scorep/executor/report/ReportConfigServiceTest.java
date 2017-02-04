package com.xz.scorep.executor.report;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ReportConfigServiceTest extends BaseTest {

    @Autowired
    private ReportConfigService reportConfigService;

    @Test
    public void saveReportConfig() throws Exception {
        ReportConfig reportConfig = new ReportConfig();
        reportConfig.setProjectId("TEST_PROJECT");
        reportConfigService.saveReportConfig(reportConfig);
    }

}