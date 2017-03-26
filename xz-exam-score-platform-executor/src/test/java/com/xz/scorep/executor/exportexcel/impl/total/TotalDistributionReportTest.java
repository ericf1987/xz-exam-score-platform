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
        String projectId = PROJECT3_ID;
        String savePath = "./target/联考学校分数分布统计.xlsx";

        // 总体统计，无需指明范围和目标
        report.generate(projectId, null, null, savePath);
    }
}