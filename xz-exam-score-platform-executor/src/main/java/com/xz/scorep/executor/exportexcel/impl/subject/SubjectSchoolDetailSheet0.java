package com.xz.scorep.executor.exportexcel.impl.subject;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class SubjectSchoolDetailSheet0 extends SheetGenerator {


    private static final Logger LOG = LoggerFactory.getLogger(SubjectSchoolDetailSheet0.class);

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private QuestService questService;

    @Autowired
    private ReportCacheInitializer reportCache;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        generateSheet0(sheetContext, studentQuery, questService, reportCache);
    }

    public static void generateSheet0(SheetContext sheetContext, StudentQuery studentQuery, QuestService questService, ReportCacheInitializer reportCache) {
        SheetTask sheetTask = sheetContext.getSheetTask();
        Target target = sheetTask.getTarget();
        String subjectId = String.valueOf(target.getId());

        sheetContext.tableSetKey("student_id");

        //////////////////////////////////////////////////////////////

        // 填充考生基本信息
        SheetContextHelper.fillStudentBasicInfo(sheetContext, studentQuery);

        // 填充科目成绩信息
        fillStudentSubjectInfo(sheetContext, studentQuery);

        // 填充题目成绩信息
        fillStudentScoreInfo(sheetContext, questService, studentQuery, reportCache);

        //////////////////////////////////////////////////////////////

        sheetContext.rowSortBy("rank_province_" + subjectId);
        sheetContext.saveData();
        sheetContext.freeze(2, 3);
    }

    private static void fillStudentScoreInfo(SheetContext sheetContext, QuestService questService, StudentQuery studentQuery, ReportCacheInitializer reportCache) {
        String projectId = sheetContext.getProjectId();
        SheetTask sheetTask = sheetContext.getSheetTask();
        Target target = sheetTask.getTarget();
        String subjectId = String.valueOf(target.getId());

        Comparator<ExamQuest> questNoComparator = (q1, q2) -> new NaturalOrderComparator().compare(q1.getQuestNo(), q2.getQuestNo());
        List<ExamQuest> quests = questService.queryQuests(projectId, subjectId);
        AtomicInteger colIndex = new AtomicInteger(10);

        List<ExamQuest> objectiveQuests = quests.stream()
                .filter(ExamQuest::isObjective).sorted(questNoComparator).collect(Collectors.toList());
        List<ExamQuest> subjectiveQuests = quests.stream()
                .filter(q -> !q.isObjective()).sorted(questNoComparator).collect(Collectors.toList());

        sheetContext.headerPut("客观题得分明细", 1, objectiveQuests.size());
        sheetContext.headerMove(Direction.DOWN);

        Range range = sheetContext.getSheetTask().getRange();
        List<String> studentList = studentQuery.getSubjectStudentList(projectId, range, subjectId);

        objectiveQuests.forEach(quest -> {
            fillStudentQuestScoreByCache(sheetContext, colIndex, quest, studentList, reportCache);
        });

        sheetContext.headerMove(Direction.UP);
        sheetContext.headerPut("主观题得分明细", 1, subjectiveQuests.size());
        sheetContext.headerMove(Direction.DOWN);
        subjectiveQuests.forEach(quest -> {
            fillStudentQuestScoreByCache(sheetContext, colIndex, quest, studentList, reportCache);
        });
    }

    private static void fillStudentQuestScoreByCache(SheetContext sheetContext, AtomicInteger colIndex, ExamQuest quest, List<String> studentList, ReportCacheInitializer reportCache) {
        String projectId = sheetContext.getProjectId();
        String scoreColName = "score_" + quest.getId();

        sheetContext.headerPut(quest.getQuestNo());
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.columnSet(colIndex.incrementAndGet(), scoreColName);

        //////////////////////////////////////////////////////////////////////////
        List<Row> rows = reportCache.queryObjectiveQuestScore(projectId, studentList, quest.getId());
        fillScore(sheetContext, quest, scoreColName, rows);

    }


    private static void fillScore(SheetContext sheetContext, ExamQuest quest, String scoreColName, List<Row> rows) {

        List<Map<String, Object>> converted =
                rows.stream().map(HashMap::new).collect(Collectors.toList());

        if (quest.isObjective()) {
            converted.forEach(map -> fixScoreRow(scoreColName, map));
            sheetContext.rowAdd(converted);
        } else {
            List<Map<String, Object>> collect = Row2MapHelper.row2Map(rows);
            sheetContext.rowAdd(collect);
        }
    }

    private static void fixScoreRow(String scoreColName, Map<String, Object> map) {
        String answer = (String) map.get("objective_answer");
        answer = StringUtil.isBlank(answer) ? "*" : answer;//避免学生题目表中答案为null
        String score = StringUtil.removeEnd(String.valueOf(map.get("score")), ".0");
        String value = score + "[" + answer + "]";
        //待测试看看有没有之前的问题(重复答案或者缺答案)
        map.put(scoreColName, value);
    }

    private static void fillStudentSubjectInfo(SheetContext sheetContext, StudentQuery studentQuery) {
        String projectId = sheetContext.getProjectId();
        SheetTask sheetTask = sheetContext.getSheetTask();
        Range range = sheetTask.getRange();
        Target target = sheetTask.getTarget();
        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        sheetContext.headerPut(subjectName, 1, 6);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("得分");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("主观题");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("客观题");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("班排名");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("校排名");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("总排名");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);

        sheetContext.columnSet(5, "total_" + subjectId);
        sheetContext.columnSet(6, "subjective_" + subjectId);
        sheetContext.columnSet(7, "objective_" + subjectId);
        sheetContext.columnSet(8, "rank_class_" + subjectId);
        sheetContext.columnSet(9, "rank_school_" + subjectId);
        sheetContext.columnSet(10, "rank_province_" + subjectId);

        sheetContext.rowAdd(studentQuery.listStudentSubjectInfo(projectId, subjectId, range));
    }

}
