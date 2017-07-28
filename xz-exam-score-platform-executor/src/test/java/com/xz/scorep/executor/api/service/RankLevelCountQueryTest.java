package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/27.
 */
public class RankLevelCountQueryTest extends BaseTest {

    @Autowired
    RankLevelCountQuery rankLevelCountQuery;

    public static final String PROJECT_ID = "430000-6c4add56e5fb42b09f9de5387dfa59c0";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "26bff727-d2e5-4ac7-b9d0-c94b7753d740";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testQuestRankLevelCount() throws Exception {
        List<Row> rows = rankLevelCountQuery.questRankLevelCount(PROJECT_ID, CLASS_ID, SUBJECT_ID);
        System.out.println(rows);
    }
}