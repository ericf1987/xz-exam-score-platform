package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/3/1
 *
 * @author yidin
 */
public class StudentObjectiveScoreAggregatorTest extends BaseTest {

    @Autowired
    private StudentObjectiveScoreAggregator studentObjectiveScoreAggregator;

    @Test
    public void aggregate() throws Exception {
        studentObjectiveScoreAggregator.aggregate(PROJECT_ID);
    }

}