package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.project.QuestService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.xz.scorep.executor.utils.SqlUtils.GroupType;
import static com.xz.scorep.executor.utils.SqlUtils.renderGroupType;

/**
 * @author by fengye on 2017/7/5.
 */
public class TopScoreRateQueryTest extends BaseTest {

    @Autowired
    TopScoreRateQuery topScoreRateQuery;

    @Autowired
    QuestService questService;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "317c7b47-587c-445b-83b8-c2887e51cec1";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testGetQuestScores() throws Exception {
        List<ExamQuest> examQuests = questService.queryQuests(PROJECT_ID, SUBJECT_ID);
        topScoreRateQuery.getQuestScoresGroup(PROJECT_ID, SUBJECT_ID, Range.CLASS, examQuests, GroupType.AVG);
    }

    @Test
    public void testGetAverage() throws Exception {
        List<ExamQuest> examQuests = questService.queryQuests(PROJECT_ID, SUBJECT_ID);
        List<Row> l1 = topScoreRateQuery.getScoreRate(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID, examQuests, false, GroupType.AVG);
        List<Row> l2 = topScoreRateQuery.getScoreRate(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, examQuests, false, GroupType.AVG);

        List<Row> rows = topScoreRateQuery.combineByRange(l1, l2);
        System.out.println(rows);

        List<Row> top = topScoreRateQuery.getTop(rows, 5, false);
        System.out.println(top.toString());
    }

    @Test
    public void test2() throws Exception {
        String s = renderGroupType(GroupType.AVG, GroupType.MAX, GroupType.MIN);
        System.out.println(s);
    }
}