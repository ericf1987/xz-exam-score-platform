package com.xz.scorep.executor.exportexcel.impl.subject;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/1
 *
 * @author yidin
 */
public class SubjectSchoolDetailReportTest extends BaseTest {

    @Autowired
    private SubjectSchoolDetailReport report;

    @Test
    public void testGenerateReport() throws Exception {
        String subjectId = "009";
        String savePath = "./target/_全校分数排名、得分明细表（地理）.xlsx";

        report.generate(
                PROJECT_ID,
                Range.school(SCHOOL_ID, SCHOOL_NAME),
                Target.subject(subjectId, "地理"),
                savePath
        );
    }
}