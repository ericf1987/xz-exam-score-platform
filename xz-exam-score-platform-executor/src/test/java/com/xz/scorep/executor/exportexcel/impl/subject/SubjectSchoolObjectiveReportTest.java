package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Author: luckylo
 * Date : 2017-03-08
 */
public class SubjectSchoolObjectiveReportTest extends BaseTest {
    @Autowired
    SubjectSchoolObjectiveReport report;

    @Test
    public void testReport() {
        Range range = Range.school("baa09eb3-5404-4915-a372-3d3a6a0e5834", "湘乡市第四中学");
        Target target = Target.subject("002", "数学");
        String savePath = "./主客观题分析（数学）.xlsx";

        report.generate(PROJECT_ID, range, target, savePath);
    }

}