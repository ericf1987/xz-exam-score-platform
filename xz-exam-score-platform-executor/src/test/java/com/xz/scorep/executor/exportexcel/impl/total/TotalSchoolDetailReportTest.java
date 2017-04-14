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
        String projectId = "430600-ae428b84d0d64e2f90e213e56b83b869";
        Range range = Range.school("5266e03d-bc8a-4c6e-b2e0-45b6bad9357f");
        String savePath = "target/全校分数排名、得分明细表(全科).xlsx";

        totalSchoolDetailReport.generate(projectId, range, null, savePath);
    }
}