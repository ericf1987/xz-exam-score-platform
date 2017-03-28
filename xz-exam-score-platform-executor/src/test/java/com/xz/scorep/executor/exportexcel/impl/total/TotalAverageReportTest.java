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

        report.generate("430100-4bc2ffbf50214ebc8ec34dd5166ef5b5",
                null, null,
                savePath);
    }
}