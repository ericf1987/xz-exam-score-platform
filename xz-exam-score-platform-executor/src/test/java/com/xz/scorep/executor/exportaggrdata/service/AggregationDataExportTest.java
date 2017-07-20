package com.xz.scorep.executor.exportaggrdata.service;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author by fengye on 2017/7/17.
 */
public class AggregationDataExportTest extends BaseTest {

    @Autowired
    AggregationDataExport aggregationDataExport;

    public static final String PROJECT_ID = "430100-9a564abc5f0044b4a470c2f146de50ab";

    @Test
    public void testExportData() throws Exception {
        aggregationDataExport.exportData(PROJECT_ID, false);
    }

}