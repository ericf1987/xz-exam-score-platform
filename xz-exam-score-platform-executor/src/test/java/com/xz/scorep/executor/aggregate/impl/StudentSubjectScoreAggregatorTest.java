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
public class StudentSubjectScoreAggregatorTest extends BaseTest {

    @Autowired
    private StudentSubjectScoreAggregator aggregator;

    @Test
    public void aggregate() throws Exception {
        this.aggregator.aggregate(
                new AggregateParameter("430300-564140e278df4e92a2a739a6f27ac391", AggregateType.Quick));
    }

}