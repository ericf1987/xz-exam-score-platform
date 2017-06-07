package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author luckylo
 * @createTime 2017-06-06.
 */
public class DistinctionValueAggregatorTest extends BaseTest {

    @Autowired
    DistinctionValueAggregator aggregator;

    @Test
    public void aggregate() throws Exception {
        aggregator.aggregate(new AggregateParameter("430300-29c4d40d93bf41a5a82baffe7e714dd9"));
    }


    @Test
    public void test(){
        List<Integer> list = new ArrayList<>(Arrays.asList(1,2,3,4,5,6,7,8,10));

        double val = list.stream()
                .sorted((a,b)->b-a)
                .limit(2)
                .mapToDouble(i1 -> i1)
                .average().getAsDouble();
        System.out.println(val);

    }

}