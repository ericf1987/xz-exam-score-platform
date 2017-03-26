package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.ScoreLevel;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Author: luckylo
 * Date : 2017-03-01 */
public class TotalAverageReportTest extends BaseTest {

    @Autowired
    TotalAverageReport report;

    @Test
    public void testGenerateReport() throws Exception {
        String projectId = "430900-9e8f3c054d72414b81cdd99bd48da695";
        String savePath = "./111.xlsx";

        report.generate(projectId,
                Range.school("1b4289a9-58e2-4560-8617-27f791f956b6", "湘潭县第四中学"),
                Target.subject("001", "语文"),
                savePath);
    }
}