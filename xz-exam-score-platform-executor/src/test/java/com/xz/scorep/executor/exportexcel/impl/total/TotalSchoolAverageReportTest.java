package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: luckylo
 * Date : 2017-03-06
 */
public class TotalSchoolAverageReportTest extends BaseTest {

    @Autowired
    TotalSchoolAverageReport report;

    @Test
    public void test() {
        Range range = Range.school("a0b6b5ac-146f-4af9-ab90-b32a28c634cb", "沅江市政通实验学校");
        report.generate("430100-c6da4bfd10134ddb9c87c601d51eb631", range, null, "./target/_平均分及三率统计（总分）.xlsx");
    }

}