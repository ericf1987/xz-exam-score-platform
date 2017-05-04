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
        String subjectId = "006";
        String savePath = "./_全校分数排名、得分明细表（生物）.xlsx";

        report.generate(
                "431300-8fdccda1e97543b5a4d8c0bfc2f3dd4e",
                Range.school("45d0c091-6370-4bf0-a27f-24aba55a2acc", SCHOOL_NAME),
                Target.subject(subjectId, "生物"),
                savePath
        );
    }
}