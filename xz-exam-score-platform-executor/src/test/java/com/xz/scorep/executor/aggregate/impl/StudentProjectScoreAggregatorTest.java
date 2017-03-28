package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/8
 *
 * @author yidin
 */
public class StudentProjectScoreAggregatorTest extends BaseTest {

    @Autowired
    private StudentProjectScoreAggregator studentProjectScoreAggregator;

    @Test
    public void aggregate() throws Exception {
        studentProjectScoreAggregator.aggregate(
                new AggregateParameter(PROJECT4_ID, AggregateType.Quick));
    }

}