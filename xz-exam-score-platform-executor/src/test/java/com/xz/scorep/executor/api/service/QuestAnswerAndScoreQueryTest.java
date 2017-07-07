package com.xz.scorep.executor.api.service;

import com.hyd.dao.Row;
import com.xz.scorep.executor.BaseTest;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.project.QuestService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/6.
 */
public class QuestAnswerAndScoreQueryTest extends BaseTest {

    @Autowired
    QuestAnswerAndScoreQuery questAnswerAndScoreQuery;

    @Autowired
    TopScoreRateQuery topScoreRateQuery;

    @Autowired
    QuestService questService;

    public static final String PROJECT_ID = "430200-13e01c025ac24c6497d916551b3ae7a6";
    public static final String PROVINCE = "430000";
    public static final String SCHOOL_ID = "200f3928-a8bd-48c4-a2f4-322e9ffe3700";
    public static final String CLASS_ID = "26bff727-d2e5-4ac7-b9d0-c94b7753d740";
    public static final String SUBJECT_ID = "001";

    @Test
    public void testQueryQuestAnswerAndScore() throws Exception {
        List<ExamQuest> examQuests = questService.queryQuests(PROJECT_ID, SUBJECT_ID);

        List<ExamQuest> objective = examQuests.stream().filter(q -> q.isObjective()).collect(Collectors.toList());
        List<ExamQuest> subjective = examQuests.stream().filter(q -> !q.isObjective()).collect(Collectors.toList());


        List<Row> l1 = questAnswerAndScoreQuery.queryObjectiveResult(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID, objective);
        List<Row> l2 = questAnswerAndScoreQuery.queryObjectiveResult(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, objective);

        //合并学校，班级客观题答案和得分情况
        List<Row> r1 = topScoreRateQuery.combineByRange(l1, l2);

        System.out.println("班级客观题结构：" + l1.toString());
        System.out.println("学校客观题结构：" + l2.toString());
        System.out.println("合并结构：" + r1.toString());

        List<Row> l3 = questAnswerAndScoreQuery.querySubjectiveResult(PROJECT_ID, SUBJECT_ID, Range.CLASS, CLASS_ID, subjective);
        List<Row> l4 = questAnswerAndScoreQuery.querySubjectiveResult(PROJECT_ID, SUBJECT_ID, Range.SCHOOL, SCHOOL_ID, subjective);

        //合并学校，班级主观题答案和得分情况
        List<Row> r2 = topScoreRateQuery.combineByRange(l3, l4);
        System.out.println("班级主观题结构：" + l3.toString());
        System.out.println("学校主观题结构：" + l4.toString());
        System.out.println("合并结构：" + r2.toString());


    }
}