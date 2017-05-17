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
        Range range = Range.school("a0b6b5ac-146f-4af9-ab90-b32a28c634cb", "沅江市南嘴中学");
        Target target = Target.subject("003", "英文");
        String savePath = "./target/_全校分数排名、得分明细表(数学).xlsx";

        report.generate("430100-c6da4bfd10134ddb9c87c601d51eb631", range, target, savePath);
    }
}