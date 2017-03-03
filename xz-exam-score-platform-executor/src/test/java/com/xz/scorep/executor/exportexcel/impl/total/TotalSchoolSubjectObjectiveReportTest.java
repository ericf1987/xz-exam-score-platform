package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/3
 *
 * @author yidin
 */
public class TotalSchoolSubjectObjectiveReportTest extends BaseTest {

    @Autowired
    private TotalSchoolSubjectObjectiveReport report;

    @Test
    public void generateReport() throws Exception {

        Range range = Range.school("baa09eb3-5404-4915-a372-3d3a6a0e5834", "湘乡市第四中学");
        Target target = Target.subject("001", "语文");
        String savePath = "./target/主客观题分析（语文）.xlsx";

        report.generate(PROJECT_ID, range, target, savePath);
    }

}