package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/6/19.
 */
public class RankLevelAggregatorTest extends BaseTest {

    @Autowired
    RankLevelSubjectAggregator rankLevelAggregator;

    @Autowired
    ReportConfigService reportConfigService;

    public static final String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

    @Test
    public void testAggregate() throws Exception {
        rankLevelAggregator.aggregate(new AggregateParameter("430300-29c4d40d93bf41a5a82baffe7e714dd9"));
    }

    @Test
    public void testAggregate1() throws Exception {
    }
}