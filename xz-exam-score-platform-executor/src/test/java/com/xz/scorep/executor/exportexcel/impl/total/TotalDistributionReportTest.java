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
        String projectId = "430100-4bc2ffbf50214ebc8ec34dd5166ef5b5";
        String savePath = "./target/联考学校分数分布统计.xlsx";

        // 总体统计，无需指明范围和目标
        report.generate(PROJECT_ID, null, null, savePath);
    }
}