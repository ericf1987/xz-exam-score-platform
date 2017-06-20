package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/20.
 */
public class RankLevelMapSubjectAggregatorTest extends BaseTest {

    @Autowired
    RankLevelMapSubjectAggregator rankLevelMapSubjectAggregator;

    public static final String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

    @Test
    public void testAggregate() throws Exception {
        rankLevelMapSubjectAggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }
}