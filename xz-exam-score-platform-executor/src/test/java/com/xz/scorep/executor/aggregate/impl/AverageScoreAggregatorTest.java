package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/8
 *
 * @author yidin
 */
public class AverageScoreAggregatorTest extends BaseTest {

    @Autowired
    private AverageScoreAggregator averageScoreAggregator;

    @Test
    public void aggregate() throws Exception {
        averageScoreAggregator.aggregate("fake_project");
    }

}