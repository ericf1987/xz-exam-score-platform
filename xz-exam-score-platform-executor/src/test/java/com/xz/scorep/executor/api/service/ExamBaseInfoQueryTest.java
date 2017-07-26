package com.xz.scorep.executor.api.service;

import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.Range;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author by fengye on 2017/7/3.
 */
public class ExamBaseInfoQueryTest extends BaseTest {

    @Autowired
    ExamBaseInfoQuery examBaseInfoQuery;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "317c7b47-587c-445b-83b8-c2887e51cec1";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testQueryCountList() throws Exception {

    }

    @Test
    public void testGetCountByRange() throws Exception {
        long countByRange = examBaseInfoQuery.getCountByRange(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID);
        System.out.println(countByRange);
    }

    @Test
    public void testGetAverageByRange() throws Exception {
        double averageByRange = examBaseInfoQuery.getGroupScoreByRange("MAX", PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID);
        System.out.println(averageByRange);
    }

    @Test
    public void testGetMaxScoreStudentName() throws Exception {
    }

    @Test
    public void testQueryBaseInfoMap() throws Exception {
        //Map<String, Object> m1 = examBaseInfoQuery.queryBaseInfoMap(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID);
        Map<String, Object> m2 = examBaseInfoQuery.queryBaseInfoMap(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID);
        //System.out.println(m1.toString());
        System.out.println(m2.toString());
    }
}