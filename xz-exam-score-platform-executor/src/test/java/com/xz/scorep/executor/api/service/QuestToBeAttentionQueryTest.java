package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.project.QuestService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author by fengye on 2017/7/7.
 */
public class QuestToBeAttentionQueryTest extends BaseTest {

    @Autowired
    QuestToBeAttentionQuery questToBeAttentionQuery;

    @Autowired
    QuestService questService;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "26bff727-d2e5-4ac7-b9d0-c94b7753d740";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testCombineByRange() throws Exception {
        List<ExamQuest> examQuests = questService.queryQuests(PROJECT_ID, SUBJECT_ID);

        List<Row> l1 = questToBeAttentionQuery.queryToBeAttentionQuest(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID, examQuests);
        List<Row> l2 = questToBeAttentionQuery.queryToBeAttentionQuest(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, examQuests);

        List<Row> r1 = questToBeAttentionQuery.combineByRange(l1, l2);

        System.out.println(r1.toString());
    }

    @Test
    public void testQueryToBeAttentionQuest() throws Exception {

    }
}