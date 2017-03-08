package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: luckylo
 * Date : 2017-03-08
 */
public class SubjectSchoolAverageReportTest extends BaseTest {
    @Autowired
    SubjectSchoolAverageReport report;

    @Test
    public void testReport() {
        Range range = Range.school("baa09eb3-5404-4915-a372-3d3a6a0e5834", "湘乡市第四中学");
        Target target = Target.subject("001", "语文");
        String savePath = "./平均分及三率统计（语文）.xlsx";

        report.generate(PROJECT_ID, range, target, savePath);
    }

}