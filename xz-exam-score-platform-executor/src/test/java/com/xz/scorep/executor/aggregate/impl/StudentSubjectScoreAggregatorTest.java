package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.aggregate.AggregateType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                new AggregateParameter("430100-354dce3ac8ef4800a1b57f81a10b8baa", AggregateType.Quick));
    }

    @Test
    public void test() {
        List<String> list = new ArrayList(Arrays.asList("004","003","005","001","002","004005006","006"));
        List<String> collect = list.stream()
                .sorted(Comparator.comparingInt(a -> a.length()))
                .collect(Collectors.toList());
        System.out.println(collect.toString());
    }

}