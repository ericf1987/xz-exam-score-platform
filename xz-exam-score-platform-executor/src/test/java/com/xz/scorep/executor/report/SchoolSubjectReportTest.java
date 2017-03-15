package com.xz.scorep.executor.report;

import com.alibaba.fastjson.JSON;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 查询学校单科详细报表
 * Author: luckylo
 * Date : 2017-03-15
 */
public class SchoolSubjectReportTest extends BaseTest {
    @Autowired
    SchoolSubjectReport report;

    @Test
    public void generateReport() throws Exception {
        String schoolId = "1b4289a9-58e2-4560-8617-27f791f956b6";
        Map<?, ?> map = report.generateReport(PROJECT_ID, schoolId, "001");
        System.out.println(JSON.toJSONString(map, true));
    }

}