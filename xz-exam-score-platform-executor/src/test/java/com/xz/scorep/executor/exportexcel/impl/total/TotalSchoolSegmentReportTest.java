package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/6
 *
 * @author yidin
 */
public class TotalSchoolSegmentReportTest extends BaseTest {

    @Autowired
    private TotalSchoolSegmentReport report;

    @Test
    public void testGenerateReport() throws Exception {
        report.generate(PROJECT_ID,
                Range.school(SCHOOL_ID, SCHOOL_NAME),
                Target.project(PROJECT_ID, "总分"),
                "./target/分数段分布统计分析（总分）.xlsx");
    }
}
