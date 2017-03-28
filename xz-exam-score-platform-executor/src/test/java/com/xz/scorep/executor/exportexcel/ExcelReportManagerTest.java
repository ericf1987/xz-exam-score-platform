package com.xz.scorep.executor.exportexcel;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/17
 *
 * @author yidin
 */
public class ExcelReportManagerTest extends BaseTest {

    @Autowired
    private ExcelReportManager excelReportManager;

    @Test
    public void generateReports() throws Exception {
        excelReportManager.generateReports(PROJECT_ID, false, false);
    }

    @Test
    public void generateReportsAsync() throws Exception {
        excelReportManager.generateReports(PROJECT_ID, true, false);
    }

}