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
        String subjectId = "001";
        String savePath = "./_全校分数排名、得分明细表（英语）.xlsx";

        report.generate(
                "430100-5d2142085fc747c9b5b230203bbfd402",
                Range.school("d988de7f-8a44-487c-9442-449c90dfd861", SCHOOL_NAME),
                Target.subject(subjectId, "语文"),
                savePath
        );
    }
}