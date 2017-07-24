package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-06-21.
 */
public class HighScoreAggregatorTest extends BaseTest {

    @Autowired HighScoreAggregator aggregator;

    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter("430000-6c4add56e5fb42b09f9de5387dfa59c0"));
    }

}