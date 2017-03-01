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
        String projectId = sheetContext.getProjectId();
        SheetTask sheetTask = sheetContext.getSheetTask();
        Range range = sheetTask.getRange();
        Target target = sheetTask.getTarget();

        String subjectId = String.valueOf(target.getId());
        String subjectName = target.getName();

        sheetContext.tableSetKey("student_id");

        //////////////////////////////////////////////////////////////

        // 填充考生基本信息
        fillStudentBasicInfo(sheetContext, projectId, range);

        // 填充科目成绩信息
        fillStudentSubjectInfo(sheetContext, projectId, range, subjectId, subjectName);

        // 填充题目成绩信息
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
            fillStudentQuestScore(sheetContext, colIndex, quest);
        });

        sheetContext.headerMove(Direction.UP);
        sheetContext.headerPut("主观题得分明细", 1, subjectiveQuests.size());
        sheetContext.headerMove(Direction.DOWN);
        subjectiveQuests.forEach(quest -> {
            fillStudentQuestScore(sheetContext, colIndex, quest);
        });



        //////////////////////////////////////////////////////////////

        sheetContext.rowSortBy("class_name", "rank_class_" + subjectId);
        sheetContext.saveData();
        sheetContext.freeze(2, 3);
    }

    private void fillStudentQuestScore(SheetContext sheetContext, AtomicInteger colIndex, ExamQuest quest) {
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

    private void fillStudentSubjectInfo(SheetContext sheetContext, String projectId, Range range, String subjectId, String subjectName) {
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

    private void fillStudentBasicInfo(SheetContext sheetContext, String projectId, Range range) {
        sheetContext.headerPut("学校名称", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("班级", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("姓名", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("A佳考号", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("学校考号", 2, 1);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.columnSet(0, "school_name");
        sheetContext.columnWidth(0, 20);
        sheetContext.columnSet(1, "class_name");
        sheetContext.columnSet(2, "student_name");
        sheetContext.columnSet(3, "exam_no");
        sheetContext.columnWidth(3, 12);
        sheetContext.columnSet(4, "school_exam_no");
        sheetContext.columnWidth(4, 14);

        sheetContext.rowAdd(studentQuery.listStudentInfo(projectId, range));
    }
}
