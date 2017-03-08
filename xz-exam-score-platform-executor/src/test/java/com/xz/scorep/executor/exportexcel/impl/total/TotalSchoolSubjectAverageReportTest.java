package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: luckylo
 * Date : 2017-03-08
 */
public class TotalSchoolSubjectAverageReportTest extends BaseTest {

    @Autowired
    TotalSchoolSubjectAverageReport report;

    @Test
    public void testReport() {
        Range range = Range.school("baa09eb3-5404-4915-a372-3d3a6a0e5834", "湘乡市第四中学");
        Target target = Target.subject("002", "数学");
        String savePath = "./_全校分数排名、得分明细表(数学).xlsx";

        report.generate(PROJECT_ID, range, target, savePath);
    }
}