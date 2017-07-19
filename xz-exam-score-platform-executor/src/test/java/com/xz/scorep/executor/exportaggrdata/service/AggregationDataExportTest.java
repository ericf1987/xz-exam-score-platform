package com.xz.scorep.executor.exportaggrdata.service;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/7/17.
 */
public class AggregationDataExportTest extends BaseTest {

    @Autowired
    AggregationDataExport aggregationDataExport;

    public static final String PROJECT_ID = "430000-6c4add56e5fb42b09f9de5387dfa59c0";

    @Test
    public void testExportData() throws Exception {
        aggregationDataExport.exportData(PROJECT_ID);
    }
}