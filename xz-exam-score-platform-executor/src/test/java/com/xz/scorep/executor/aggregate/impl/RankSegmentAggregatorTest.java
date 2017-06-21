package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/6/21.
 */
public class RankSegmentAggregatorTest extends BaseTest{

    @Autowired
    RankSegmentAggregator rankSegmentAggregator;

    public static final String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

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