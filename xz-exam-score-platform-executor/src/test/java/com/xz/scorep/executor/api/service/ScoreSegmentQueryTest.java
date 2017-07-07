package com.xz.scorep.executor.api.service;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;

/**
 * @author by fengye on 2017/7/7.
 */
public class ScoreSegmentQueryTest extends BaseTest {

    @Autowired
    ScoreSegmentQuery scoreSegmentQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "26bff727-d2e5-4ac7-b9d0-c94b7753d740";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testGetCountByScoreSegment() throws Exception {
        LinkedHashMap<Integer, Integer> countByScoreSegment = scoreSegmentQuery.getCountByScoreSegment(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID);
        LinkedHashMap<Integer, Integer> countByScoreSegment1 = scoreSegmentQuery.getCountByScoreSegment(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID);
        System.out.println(countByScoreSegment.toString());
        System.out.println(countByScoreSegment1.toString());
    }
}