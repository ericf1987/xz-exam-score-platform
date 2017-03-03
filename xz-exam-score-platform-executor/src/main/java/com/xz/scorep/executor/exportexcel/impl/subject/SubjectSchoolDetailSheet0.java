package com.xz.scorep.executor.exportexcel.impl.subject;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NaturalOrderComparator;
import com.xz.ajiaedu.common.lang.StringUtil;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class SubjectSchoolDetailSheet0 extends SheetGenerator {

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private QuestService questService;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        generateSheet0(sheetContext, studentQuery, questService);
    }

    public static void generateSheet0(SheetContext sheetContext, StudentQuery studentQuery, QuestService questService) {
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
        fillStudentScoreInfo(sheetContext, questService, studentQuery);

        //////////////////////////////////////////////////////////////

        sheetContext.rowSortBy("class_name", "rank_class_" + subjectId);
        sheetContext.saveData();
        sheetContext.freeze(2, 3);
    }

    private static void fillStudentScoreInfo(SheetContext sheetContext, QuestService questService, StudentQuery studentQuery) {
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
        objectiveQuests.forEach(quest -> {
            fillStudentQuestScore(sheetContext, colIndex, quest, studentQuery);
        });

        sheetContext.headerMove(Direction.UP);
        sheetContext.headerPut("主观题得分明细", 1, subjectiveQuests.size());
        sheetContext.headerMove(Direction.DOWN);
        subjectiveQuests.forEach(quest -> {
            fillStudentQuestScore(sheetContext, colIndex, quest, studentQuery);
        });
    }

    private static void fillStudentQuestScore(SheetContext sheetContext, AtomicInteger colIndex, ExamQuest quest, StudentQuery studentQuery) {
        String projectId = sheetContext.getProjectId();
        Range range = sheetContext.getSheetTask().getRange();
        String scoreColName = "score_" + quest.getId();

        sheetContext.headerPut(quest.getQuestNo());
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.columnSet(colIndex.incrementAndGet(), scoreColName);

        List<Row> rows = studentQuery.listStudentQuestScore(projectId, quest.getId(), range);
        if (quest.isObjective()) {
            rows.forEach(row -> {
                String answer = row.getString("objective_answer");
                String score = StringUtil.removeEnd(row.getString(scoreColName), ".0");
                row.put(scoreColName, score + "[" + answer + "]");
            });
        }

        sheetContext.rowAdd(rows);
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
