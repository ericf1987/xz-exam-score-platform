package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/27
 *
 * @author yidin
 */
public class TotalDistributionReportTest extends BaseTest {

    @Autowired
    private TotalDistributionReport report;

    @Test
    public void testGenerateReport() throws Exception {
        String projectId = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        String savePath = "./target/联考学校分数分布统计.xlsx";

        // 总体统计，无需指明范围和目标
        report.generate(projectId, null, null, savePath);
    }
}