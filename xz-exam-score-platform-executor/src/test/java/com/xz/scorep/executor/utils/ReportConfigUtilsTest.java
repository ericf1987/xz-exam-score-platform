package com.xz.scorep.executor.utils;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-07-29.
 */
public class ReportConfigUtilsTest extends BaseTest {

    @Autowired
    ReportConfigService reportConfigService;

    @Test
    public void convertReportConfig() throws Exception {
        String projectId = "430000-6c4add56e5fb42b09f9de5387dfa59c0";
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        List<String> list = ReportConfigUtils.convertReportConfig(reportConfig);
        System.out.println(list);
        String string = String.join(",",list);
        System.out.println(string);
    }

}