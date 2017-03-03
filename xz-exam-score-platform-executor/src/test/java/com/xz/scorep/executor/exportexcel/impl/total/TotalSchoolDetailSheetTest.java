package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Author: luckylo
 * Date : 2017-03-03
 */
public class TotalSchoolDetailSheetTest extends BaseTest {

    @Autowired
    TotalSchoolDetailReport report;

    @Test
    public void testGenerateReport() {
        String projectId = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        String savePath = "./_全校分数排名、得分明细表(全科).xlsx";

        report.generate(projectId,
                Range.school("1b4289a9-58e2-4560-8617-27f791f956b6", "湘潭县第四中学"),
                Target.subject("001", "总分"),
                savePath);
    }
}