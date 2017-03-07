package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Author: luckylo
 * Date : 2017-03-06
 */
public class TotalSchoolAverageReportTest extends BaseTest {

    @Autowired
    TotalSchoolAverageReport report;

    @Test
    public void test() {
        Range range = Range.school("1b4289a9-58e2-4560-8617-27f791f956b6","湘潭县第四中学");
        Target target = Target.subject("001","语文");
        report.generate(PROJECT_ID,range,target,"./！平均分及三率统计（总分）.xlsx");
    }

}