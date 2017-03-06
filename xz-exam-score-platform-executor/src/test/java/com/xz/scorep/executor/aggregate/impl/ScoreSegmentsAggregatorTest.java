package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/9
 *
 * @author yidin
 */
public class ScoreSegmentsAggregatorTest extends BaseTest {

    @Autowired
    private ScoreSegmentsAggregator scoreSegmentsAggregator;

    @Test
    public void aggregate() throws Exception {
        scoreSegmentsAggregator.aggregate(PROJECT_ID);
    }

}