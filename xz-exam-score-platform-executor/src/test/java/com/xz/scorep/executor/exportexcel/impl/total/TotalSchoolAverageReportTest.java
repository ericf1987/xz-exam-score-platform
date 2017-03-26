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
        Range range = Range.school("508de5f9-0103-43e3-b29f-6a24696f639d", "沅江市政通实验学校");
        report.generate(PROJECT3_ID, range, null, "./target/_平均分及三率统计（总分）.xlsx");
    }

}