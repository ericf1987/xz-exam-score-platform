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

        Range range = Range.school("002e02d6-c036-4780-85d4-e54e3f1fbf9f", "111");
        Target target = Target.subject("007008009", "文科综合");
        String savePath = "./target/主客观题分析（" + target.getName() + "）.xlsx";

        report.generate("430300-c582131e66b64fe38da7d0510c399ec4", range, target, savePath);
    }

}