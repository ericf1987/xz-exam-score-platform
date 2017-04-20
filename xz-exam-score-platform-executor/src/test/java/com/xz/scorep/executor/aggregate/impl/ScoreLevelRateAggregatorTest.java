package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateType;
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
        scoreLevelRateAggregator.aggregate(new AggregateParameter("430300-2ded1a538def48c08d1d82014acf55ba", AggregateType.Quick));
    }

}