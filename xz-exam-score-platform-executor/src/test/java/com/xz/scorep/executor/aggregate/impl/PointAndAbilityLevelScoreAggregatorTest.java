package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/27.
 */
public class PointAndAbilityLevelScoreAggregatorTest extends BaseTest {

    @Autowired
    PointAndAbilityLevelScoreAggregator pointAndAbilityLevelScoreAggregator;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";

    @Test
    public void testAggregate() throws Exception {
        pointAndAbilityLevelScoreAggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }
}