package com.xz.scorep.executor.aggritems;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Target;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/5/7.
 */
public class FullScoreQueryTest extends BaseTest {

    @Autowired
    FullScoreQuery fullScoreQuery;

    public static final String PROJECT_ID = "431300-8fdccda1e97543b5a4d8c0bfc2f3dd4e";

    @Test
    public void testGetFullScore() throws Exception {
        Double fullScore = fullScoreQuery.getFullScore(PROJECT_ID, Target.project(PROJECT_ID));
        System.out.println(fullScore);
    }
}