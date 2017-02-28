package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/28
 *
 * @author yidin
 */
public class RankAggregatorTest extends BaseTest {

    @Autowired
    private RankAggregator rankAggregator;

    @Test
    public void aggregate() throws Exception {
        rankAggregator.aggregate(PROJECT_ID);
    }

}