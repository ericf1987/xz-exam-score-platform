package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/6
 *
 * @author yidin
 */
public class TotalSchoolSubjectSegmentReportTest extends BaseTest {

    @Autowired
    private TotalSchoolSubjectSegmentReport report;

    @Test
    public void testGenerateReport() throws Exception {
        String savePath = "./target/分数段分布统计分析（" + TARGET_SUBJECT_001.getName() + "）.xlsx";
        report.generate(PROJECT_ID, Range.school(SCHOOL_ID, SCHOOL_NAME), TARGET_SUBJECT_001, savePath);
    }
}