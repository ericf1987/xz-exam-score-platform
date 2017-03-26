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

        Range range = Range.school("1d4ed032-1e55-4d74-a2fc-92c1e1f6c2f9", "沅江市第一中学");
        Target target = Target.subject("002", "数学");
        String savePath = "./target/主客观题分析（数学）.xlsx";

        report.generate(PROJECT3_ID, range, target, savePath);
    }

}