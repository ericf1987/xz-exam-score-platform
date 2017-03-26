package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/7
 *
 * @author yidin
 */
public class AllPassOrFailAggregatorTest extends BaseTest {

    @Autowired
    AllPassOrFailAggregator allPassOrFailAggregator;

    @Test
    public void testAggregate() throws Exception {
        allPassOrFailAggregator.aggregate(new AggregateParameter(PROJECT3_ID));
    }

}