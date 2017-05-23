package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author luckylo
 */
public class QuestAverageAggregatorTest extends BaseTest{

    @Autowired
    QuestAverageAggregator averageAggregator;

    @Test
    public void aggregate() throws Exception {
        averageAggregator.aggregate(new AggregateParameter("430300-29c4d40d93bf41a5a82baffe7e714dd9"));
    }

}