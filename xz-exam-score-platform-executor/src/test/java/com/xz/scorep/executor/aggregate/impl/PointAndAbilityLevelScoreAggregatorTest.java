package com.xz.scorep.executor.aggregate.impl;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.aggregate.AggregateParameter;
import com.xz.scorep.executor.db.DAOFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/6/27.
 */
public class PointAndAbilityLevelScoreAggregatorTest extends BaseTest {

    @Autowired
    PointAndAbilityLevelScoreAggregator pointAndAbilityLevelScoreAggregator;

    public static final String PROJECT_ID = "430000-6c4add56e5fb42b09f9de5387dfa59c0";

    @Autowired
    DAOFactory daoFactory;

    @Test
    public void testAggregate() throws Exception {
        pointAndAbilityLevelScoreAggregator.aggregate(new AggregateParameter(PROJECT_ID));
    }

    @Test
    public void testDoAggregate() throws Exception {
        pointAndAbilityLevelScoreAggregator.doAggregate(PROJECT_ID, daoFactory.getProjectDao(PROJECT_ID), "007");
    }
}