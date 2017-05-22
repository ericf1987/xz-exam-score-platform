package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author luckylo
 */
public class MaxMInScoreAggregatorTest extends BaseTest {

    @Autowired
    MaxMinScoreAggregator maxMInScoreAggregator;

    @Test
    public void aggregate() throws Exception {
        maxMInScoreAggregator.aggregate(new AggregateParameter("430300-29c4d40d93bf41a5a82baffe7e714dd9"));
    }

}