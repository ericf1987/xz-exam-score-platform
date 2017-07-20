package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateOrder;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateType;
import com.xz.scorep.executor.aggregate.AggregateTypes;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * (description)
 * created at 2017/3/7
 *
 * @author yidin
 */
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(15)
@Component
public class AllExcellentGoodFailAggregatorTest extends BaseTest {

    @Autowired
    AllExcellentGoodFailAggregator allExcellentGoodFailAggregator;


    @Test
    public void testAggregate() throws Exception {

        String projectId = "431100-8b3c36cdf2fe442cbc96a50021a2dc2b";
        //String projectId = "430100-59f2afde35a44b92ae3d2c4c10bd4075";
        allExcellentGoodFailAggregator.aggregate(new AggregateParameter(projectId));
    }

}