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

        Range range = Range.school("d988de7f-8a44-487c-9442-449c90dfd861", "111");
        Target target = Target.subject("004", "物理");
        String savePath = "./target/主客观题分析（" + target.getName() + "）.xlsx";

        report.generate("430100-354dce3ac8ef4800a1b57f81a10b8baa", range, target, savePath);
    }

}