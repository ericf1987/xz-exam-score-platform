package com.xz.scorep.executor.report;

import com.alibaba.fastjson.JSON;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * (description)
 * created at 2017/2/13
 *
 * @author yidin
 */
public class SchoolOverviewReportTest extends BaseTest {

    @Autowired
    private SchoolOverviewReport schoolOverviewReport;

    @Test
    public void generateSchoolReport() throws Exception {
        String projectId = "fake_project";
        String schoolId = "207ae6fd_2e1d_41d7_96e8_d2bb68de3cb4";
        Map<?, ?> map = this.schoolOverviewReport.generateReport(projectId, schoolId, "001");
        System.out.println(JSON.toJSONString(map, true));
    }

}