package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/22.
 */
public class QuestTypeScoreAggregatorTest extends BaseTest {

    @Autowired
    QuestTypeScoreAggregator questTypeScoreAggregator;

    @Test
    public void testAggregate() throws Exception {
        questTypeScoreAggregator.aggregate(new AggregateParameter("430200-13e01c025ac24c6497d916551b3ae7a6"));
    }
}