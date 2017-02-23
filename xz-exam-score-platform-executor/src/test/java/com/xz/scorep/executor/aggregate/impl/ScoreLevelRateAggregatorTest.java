package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * (description)
 * created at 2017/2/11
 *
 * @author yidin
 */
public class ScoreLevelRateAggregatorTest extends BaseTest {

    @Autowired
    private ScoreLevelRateAggregator scoreLevelRateAggregator;

    @Test
    public void aggregate() throws Exception {
        String projectId = "430300-9cef9f2059ce4a36a40a7a60b07c7e00";
        scoreLevelRateAggregator.aggregate(projectId);
    }

}