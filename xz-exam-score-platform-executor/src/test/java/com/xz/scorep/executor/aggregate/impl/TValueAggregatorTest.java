package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author luckylo
 * @createTime 2017-06-05.
 */
public class TValueAggregatorTest extends BaseTest {

    @Autowired
    TValueAggregator aggregator;


    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter("430200-13e01c025ac24c6497d916551b3ae7a6"));
    }

}