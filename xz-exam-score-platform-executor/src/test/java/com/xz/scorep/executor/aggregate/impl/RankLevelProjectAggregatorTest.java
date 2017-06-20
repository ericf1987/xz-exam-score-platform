package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.project.SubjectService;
import org.apache.commons.lang.BooleanUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/20.
 */
public class RankLevelProjectAggregatorTest extends BaseTest {

    public static final String PROJECT_ID = "430300-29c4d40d93bf41a5a82baffe7e714dd9";

    @Autowired
    RankLevelProjectAggregator rankLevelProjectAggregator;

    @Test
    public void testAggregate() throws Exception {
        rankLevelProjectAggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }
}