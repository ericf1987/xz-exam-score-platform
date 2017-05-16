package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author by fengye on 2017/5/15.
 */
public class ScoreRateAggregatorTest extends BaseTest {

    @Autowired
    ScoreRateAggregator scoreRateAggregator;

    public static final String PROJECT_ID = "431300-8fdccda1e97543b5a4d8c0bfc2f3dd4e";

    @Test
    public void testAggregate() throws Exception {
        AggregateParameter param = new AggregateParameter(PROJECT_ID, AggregateType.Quick);
        scoreRateAggregator.aggregate(param);
    }
}