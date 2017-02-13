package com.xz.scorep.executor.reportconfig;

import com.alibaba.fastjson.JSON;
import com.xz.ajiaedu.common.lang.MapBuilder;
import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 * (description)
 * created at 2017/2/4
 *
 * @author yidin
 */
public class ReportConfigServiceTest extends BaseTest {

    @Autowired
    private ReportConfigService reportConfigService;

    @Test
    public void saveReportConfig() throws Exception {
        ReportConfig reportConfig = new ReportConfig();
        reportConfig.setScoreLevels(JSON.toJSONString(MapBuilder
                .start(Keys.ScoreLevel.Fail.name(), 0d)
                .and(Keys.ScoreLevel.Pass.name(), 0.6)
                .and(Keys.ScoreLevel.Good.name(), 0.7)
                .and(Keys.ScoreLevel.Excellent.name(), 0.8)
                .get()));
        reportConfig.setRankLevels(JSON.toJSONString(MapBuilder
                .start("A", 0.1)
                .and("B", 0.2)
                .and("C", 0.3)
                .and("D", 0.4)
                .get()));
        reportConfig.setRankSegmentCount(30);
        reportConfig.setTopStudentRate(0.05);
        reportConfig.setHighScoreRate(0.1);
        reportConfig.setCombineCategorySubjects("false");
        reportConfig.setSeparateCategorySubjects("false");
        reportConfig.setCollegeEntryLevelEnabled("false");
        reportConfig.setRankLevelCombines(JSON.toJSONString(Collections.emptyList()));
        reportConfig.setCollegeEntryLevel(JSON.toJSONString(Collections.emptyList()));
        reportConfig.setProjectId("[DEFAULT]");
        reportConfigService.saveReportConfig(reportConfig);
    }

}