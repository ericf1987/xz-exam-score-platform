package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/6/21.
 */
public class RankSegmentAggregatorTest extends BaseTest{

    @Autowired
    RankSegmentAggregator rankSegmentAggregator;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";

    @Autowired
    ReportConfigService reportConfigService;

    @Test
    public void test() {
        ReportConfig reportConfig = reportConfigService.queryReportConfig(PROJECT_ID);

        int rankSegmentCount = reportConfig.getRankSegmentCount();

        System.out.println(rankSegmentCount);
    }

    @Test
    public void testGetRankSegmentParam() throws Exception {
        double[] rankSegmentParam = rankSegmentAggregator.getRankSegmentParam(PROJECT_ID);
        for (int i = 0; i < rankSegmentParam.length; i++) {
            System.out.println(rankSegmentParam[i]);
        }
        System.out.println("----------------------------------------------");
        double[] doubles = rankSegmentAggregator.queryRankInSegment(rankSegmentParam, 39);
        for (int i = 0; i < doubles.length; i++) {
            System.out.println(doubles[i]);
        }
    }

    @Test
    public void testAggregate() throws Exception {
        rankSegmentAggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }
}