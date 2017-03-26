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
        Range range = Range.school("04b91813-cc5e-4139-aa71-871ce82c8b41", "沅江市南嘴中学");
        Target target = Target.subject("001", "语文");
        String savePath = "./target/_全校分数排名、得分明细表(数学).xlsx";

        report.generate(PROJECT3_ID, range, target, savePath);
    }
}