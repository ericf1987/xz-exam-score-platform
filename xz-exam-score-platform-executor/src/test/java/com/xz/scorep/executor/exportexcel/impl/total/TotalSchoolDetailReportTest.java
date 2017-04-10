package com.xz.scorep.executor.exportexcel.impl.total;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yiding_he
 */
public class TotalSchoolDetailReportTest extends BaseTest {

    @Autowired
    private TotalSchoolDetailReport totalSchoolDetailReport;

    @Test
    public void testGenerateReport() throws Exception {
        String projectId = "430300-29c4d40d93bf41a5a82baffe7e714dd9";
        Range range = Range.school("");
        String savePath = "target/全校分数排名、得分明细表(全科).xlsx";

        totalSchoolDetailReport.generate(projectId, range, null, savePath);
    }
}