package com.xz.scorep.executor.aggregate.impl;

import com.xz.ajiaedu.common.lang.Counter;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/28.
 */
public class PointAndAbilityLevelSumToRangeAggregatorTest extends BaseTest {

    @Autowired
    PointAndAbilityLevelSumToRangeAggregator pointAndAbilityLevelSumToRangeAggregator;

    @Autowired
    public static final Logger LOG = LoggerFactory.getLogger(PointAndAbilityLevelSumToRangeAggregatorTest.class);

    @Test
    public void testAggregate() throws Exception {
/*        String sql = pointAndAbilityLevelSumToRangeAggregator.getSqlByRangeName(Range.CLASS, "score_point");
        System.out.println(sql);*/

        pointAndAbilityLevelSumToRangeAggregator.aggregate(new AggregateParameter("430200-13e01c025ac24c6497d916551b3ae7a6"));
    }

    @Test
    public void test1() throws Exception{
        Counter counter = new Counter(500, i -> LOG.info("counter:" + i / 0));
        int count = 100;
        for (int j = 0; j < count; j++) {
            counter.incre();
        }
        counter.finish();
    }
}