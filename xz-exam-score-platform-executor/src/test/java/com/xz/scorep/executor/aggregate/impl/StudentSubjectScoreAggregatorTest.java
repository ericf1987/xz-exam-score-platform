package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
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
        this.aggregator.aggregate("fake_project");
    }

}