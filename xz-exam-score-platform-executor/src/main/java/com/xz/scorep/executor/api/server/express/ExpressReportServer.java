package com.xz.scorep.executor.api.server.express;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.ajia.Param;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.ajiaedu.common.lang.Result;
import com.xz.scorep.executor.api.annotation.Function;
import com.xz.scorep.executor.api.annotation.Parameter;
import com.xz.scorep.executor.api.annotation.Type;
import com.xz.scorep.executor.api.server.Server;
import com.xz.scorep.executor.api.service.*;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.xz.scorep.executor.utils.SqlUtils.GroupType;

/**
 * 快速报表接口
 *
 * @author by fengye on 2017/7/3.
 */
@Function(description = "查询学生考试情况", parameters = {
        @Parameter(name = "projectId", type = Type.String, description = "考试项目ID", required = true),
        @Parameter(name = "subjectId", type = Type.String, description = "科目ID", required = true),
        @Parameter(name = "schoolId", type = Type.String, description = "学校ID", required = true),
        @Parameter(name = "classId", type = Type.String, description = "班级ID", required = true)
})
@Service
public class ExpressReportServer implements Server {

    @Autowired
    ExamBaseInfoQuery examBaseInfoQuery;

    @Autowired
    TopStudentRankAndScoreQuery topStudentRankAndScoreQuery;

    @Autowired
    ScoreSegmentQuery scoreSegmentQuery;

    @Autowired
    SAndOQuestQuery sAndOQuestQuery;

    @Autowired
    TopScoreRateQuery topScoreRateQuery;

    @Autowired
    QuestService questService;

    @Autowired
    QuestAnswerAndScoreQuery questAnswerAndScoreQuery;

    @Autowired
    QuestToBeAttentionQuery questToBeAttentionQuery;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    SubjectService subjectService;

    public static final int TOP_COUNT = 5;

    @Override
    public Result execute(Param param) {

        String projectId = param.getString("projectId");
        String subjectId = param.getString("subjectId");
        String schoolId = param.getString("schoolId");
        String classId = param.getString("classId");

        //学校，班级名称
        ProjectSchool school = schoolService.findSchool(projectId, schoolId);
        ProjectClass clazz = classService.findClass(projectId, classId);
        ExamSubject subject = subjectService.findSubject(projectId, subjectId);
        Map<String, String> schoolAndClass = new HashMap<>();
        schoolAndClass.put("schoolName", school.getName());
        schoolAndClass.put("className", clazz.getName());
        schoolAndClass.put("subjectName", subject.getName());

        List<ExamQuest> examQuests = questService.queryQuests(projectId, subjectId);
        List<ExamQuest> objectiveQuests = examQuests.stream().filter(ExamQuest::isObjective).collect(Collectors.toList());
        List<ExamQuest> subjectiveQuests = examQuests.stream().filter(q -> !q.isObjective()).collect(Collectors.toList());

        //1.考试基本情况
        Map<String, Object> baseInfoMap = new HashMap<>();
        Map<String, Object> schoolBaseInfo = examBaseInfoQuery.queryBaseInfoMap(projectId, subjectId, Range.SCHOOL, schoolId);
        Map<String, Object> classBaseInfo = examBaseInfoQuery.queryBaseInfoMap(projectId, subjectId, Range.CLASS, classId);


        baseInfoMap.put("schoolBaseInfo", schoolBaseInfo);
        baseInfoMap.put("classBaseInfo", classBaseInfo);
        //2.班级TOP5
        //前五名
        List<Map<String, Object>> top = topStudentRankAndScoreQuery.queryTopStudent(projectId, subjectId, Range.CLASS, classId, true, TOP_COUNT);
        //后五名
        List<Map<String, Object>> bottom = topStudentRankAndScoreQuery.queryTopStudent(projectId, subjectId, Range.CLASS, classId, false, TOP_COUNT);

        Map<String, Object> top5Map = new HashMap<>();
        top5Map.put("top", top);
        top5Map.put("bottom", bottom);
        //3.分段人数统计
        LinkedHashMap<Integer, Integer> countByScoreSegment = scoreSegmentQuery.getCountByScoreSegment(projectId, subjectId, Range.CLASS, classId);

        //4.主客观题情况
        //客观题情况
        Map<String, Object> class_objectiveMap = sAndOQuestQuery.getSAndOMap(projectId, subjectId, Range.CLASS, classId, true);
        Map<String, Object> school_objectiveMap = sAndOQuestQuery.getSAndOMap(projectId, subjectId, Range.SCHOOL, schoolId, true);

        //主观题情况
        Map<String, Object> class_subjectiveMap = sAndOQuestQuery.getSAndOMap(projectId, subjectId, Range.CLASS, classId, false);
        Map<String, Object> school_subjectiveMap = sAndOQuestQuery.getSAndOMap(projectId, subjectId, Range.SCHOOL, schoolId, false);

        Map<String, Object> sAndOStatusMap = new HashMap<>();
        sAndOStatusMap.put("classObjectiveMap", class_objectiveMap);
        sAndOStatusMap.put("schoolObjectiveMap", school_objectiveMap);
        sAndOStatusMap.put("classSubjectiveMap", class_subjectiveMap);
        sAndOStatusMap.put("schoolSubjectiveMap", school_subjectiveMap);

        //5.得分较高的题目
        List<Row> school_scoreRate = topScoreRateQuery.getScoreRate(projectId, subjectId, Range.SCHOOL, schoolId, examQuests, true, GroupType.AVG);
        List<Row> class_scoreRate = topScoreRateQuery.getScoreRate(projectId, subjectId, Range.CLASS, classId, examQuests, true, GroupType.AVG);
        List<Row> combined_scoreRate = topScoreRateQuery.combineByRange(class_scoreRate, school_scoreRate);

        //较高top5
        List<Row> score_rate_top = topScoreRateQuery.getTop(combined_scoreRate, TOP_COUNT, false);
        //较低top5
        List<Row> score_rate_bottom = topScoreRateQuery.getTop(combined_scoreRate, TOP_COUNT, true);

        Map<String, Object> scoreRateMap = new HashMap<>();
        scoreRateMap.put("top5", score_rate_top);
        scoreRateMap.put("bottom5", score_rate_bottom);

        //6.客观题，主观题详情
        List<Row> class_objective_detail = questAnswerAndScoreQuery.queryObjectiveResult(projectId, subjectId, Range.CLASS, classId, objectiveQuests);
        List<Row> school_objective_detail = questAnswerAndScoreQuery.queryObjectiveResult(projectId, subjectId, Range.SCHOOL, schoolId, objectiveQuests);
        List<Row> objective_detail = topScoreRateQuery.combineByRange(class_objective_detail, school_objective_detail);

        List<Row> class_subjective_detail = questAnswerAndScoreQuery.querySubjectiveResult(projectId, subjectId, Range.CLASS, classId, subjectiveQuests);
        List<Row> school_subjective_detail = questAnswerAndScoreQuery.querySubjectiveResult(projectId, subjectId, Range.SCHOOL, schoolId, subjectiveQuests);
        List<Row> subjective_detail = topScoreRateQuery.combineByRange(class_subjective_detail, school_subjective_detail);

        //转化为百分号
        objective_detail.stream().forEach(o -> {
            o.put("rate", DoubleUtils.toPercent(o.getDouble("rate", 0)));
            o.put("parent_rate", DoubleUtils.toPercent(o.getDouble("parent_rate", 0)));
        });

        subjective_detail.stream().forEach(s -> {
            s.put("rate", DoubleUtils.toPercent(s.getDouble("rate", 0)));
            s.put("parent_rate", DoubleUtils.toPercent(s.getDouble("parent_rate", 0)));
        });

        Comparator comparator = new NaturalOrderComparator();

        //按照题号升序排列
        Collections.sort(objective_detail, (o1, o2) -> comparator.compare(o1.getString("questno"), o2.getString("questno")));
        Collections.sort(subjective_detail, (o1, o2) -> comparator.compare(o1.getString("questno"), o2.getString("questno")));

        Map<String, Object> quest_detail = new HashMap<>();
        quest_detail.put("objectiveDetail", objective_detail);
        quest_detail.put("subjectiveDetail", subjective_detail);

        //7.值得关注的小题
        List<Row> class_tb_attention = questToBeAttentionQuery.queryToBeAttentionQuest(projectId, subjectId, Range.CLASS, classId, examQuests);
        List<Row> school_tb_attention = questToBeAttentionQuery.queryToBeAttentionQuest(projectId, subjectId, Range.SCHOOL, schoolId, examQuests);
        List<Row> attentionQuestList = questToBeAttentionQuery.combineByRange(class_tb_attention, school_tb_attention);

        //标记主观题或客观题
        attentionQuestList.stream().forEach(q -> {
            ExamQuest quest = questService.findQuest(projectId, q.getString("questid"));
            q.put("quest_type", quest.isObjective() ? "客观题" : "主观题");
        });


        Map<String, Object> toBeAttention = new HashMap<>();
        toBeAttention.put("attentionQuests", attentionQuestList);

        return Result.success().set("schoolAndClass", schoolAndClass)
                .set("baseInfoMap", baseInfoMap)
                .set("top5Map", top5Map)
                .set("countByScoreSegment", countByScoreSegment)
                .set("sAndOStatusMap", sAndOStatusMap)
                .set("scoreRateMap", scoreRateMap)
                .set("quest_detail", quest_detail)
                .set("toBeAttention", toBeAttention);
    }
}
