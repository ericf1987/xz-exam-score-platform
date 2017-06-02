package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Created by wanna on 2017-06-02.
 */
public class MedianAggregatorTest extends BaseTest {
    @Autowired
    private MedianAggregator aggregator;

    @Test
    public void aggregate() throws Exception {
        AggregateParameter parameter = new AggregateParameter("430300-29c4d40d93bf41a5a82baffe7e714dd9");
        aggregator.aggregate(parameter);
    }


}