package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/6
 *
 * @author yidin
 */
public class ObjectiveOptionAggregatorTest extends BaseTest {

    @Autowired
    private ObjectiveOptionAggregator aggregator;

    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter("430900-9e8f3c054d72414b81cdd99bd48da695"));
    }

}