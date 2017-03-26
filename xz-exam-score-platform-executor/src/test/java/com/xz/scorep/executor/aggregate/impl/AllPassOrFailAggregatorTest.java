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
        allPassOrFailAggregator.aggregate(new AggregateParameter("430900-9e8f3c054d72414b81cdd99bd48da695"));
    }

}