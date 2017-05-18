package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author luckylo
 */
public class StdDeviationAggregatorTest extends BaseTest{
    @Autowired
    StdDeviationAggregator stdDeviationAggregator;

    private String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

    @Test
    public void aggregate() throws Exception {
        AggregateParameter param = new AggregateParameter(PROJECT_ID, AggregateType.Quick);
        stdDeviationAggregator.aggregate(param);
    }

}