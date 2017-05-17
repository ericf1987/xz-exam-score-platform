package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: luckylo
 * Date : 2017-03-01 */
public class TotalAverageReportTest extends BaseTest {

    @Autowired
    TotalAverageReport report;

    @Test
    public void testGenerateReport() throws Exception {
        String savePath = "./target/联考学校平均分统计分析.xlsx";

        report.generate("430100-c6da4bfd10134ddb9c87c601d51eb631",
                null, null,
                savePath);
    }
}