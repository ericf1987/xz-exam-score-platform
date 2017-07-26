package com.xz.scorep.executor.api.service;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.utils.SqlUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author by fengye on 2017/7/4.
 */
public class SAndOQuestQueryTest extends BaseTest {

    @Autowired
    SAndOQuestQuery sAndOQuestQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "317c7b47-587c-445b-83b8-c2887e51cec1";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testGetGroupTypeScore() throws Exception {
        double groupTypeScore = sAndOQuestQuery.getGroupTypeScore(SqlUtils.GroupType.AVG, PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, false);
        System.out.println(groupTypeScore);
        System.out.println(sAndOQuestQuery.getQueryMaxScoreStudent(SqlUtils.GroupType.MAX, PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, false));
    }

    @Test
    public void testGetSAndOMap() throws Exception {
        //Map<String, Object> m1 = sAndOQuestQuery.getSAndOMap(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, false);
        Map<String, Object> m2 = sAndOQuestQuery.getSAndOMap(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID, false);
        Map<String, Object> m3 = sAndOQuestQuery.getSAndOMap(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID, true);
        //System.out.println(m1.toString());
        System.out.println(m2.toString());
        System.out.println(m3.toString());
    }
}