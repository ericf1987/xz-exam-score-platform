package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.Row;
import com.xz.scorep.executor.aggritems.ScoreQuery;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class TotalBasicDataSheets extends SheetGenerator {

    private static final Range PROVINCE_RANGE = Range.province("430000");

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private ScoreQuery scoreQuery;

    @Override
    protected void generateSheet(SheetContext context) throws Exception {

        setupColumns(context);
        fillHeader(context);
        fillData(context);

        context.rowSortBy("area", "school_name", "class_name");
        context.saveData();
    }

    private void setupColumns(SheetContext context) {

        // 1. 考生基本信息字段
        String[] stuInfoColumns = {
                "exam_no", "school_exam_no", "student_name", "area", "school_name", "class_name"
        };

        for (int i = 0; i < stuInfoColumns.length; i++) {
            context.columnSet(i, stuInfoColumns[i]);
        }

        // 2. 各科成绩和排名字段
        // 查询科目列表并在第一个位置加上“全科”科目
        List<ExamSubject> subjects = subjectService.listSubjects(context.getProjectId());
        subjects.add(0, fakeTotalSubject());

        // 设置每个科目的分数和排名字段名称
        for (int i = 0; i < subjects.size(); i++) {
            ExamSubject subject = subjects.get(i);
            int columnIndex = i * 2 + stuInfoColumns.length;
            context.columnSet(columnIndex, "score_" + subject.getId());
            context.columnSet(columnIndex + 1, "rank_" + subject.getId());
        }
    }

    private void fillHeader(SheetContext context) {

        // 考生基本信息表头
        Stream.of("考号", "学校考号", "姓名", "市区", "学校", "班级")
                .forEach(text -> {
                    context.headerPut(text, 2, 1);
                    context.headerMove(Direction.RIGHT);
                });

        // 科目列表表头：科目列表前面加个全科
        Stream<ExamSubject> subjectStreamWithTotal =
                getSubjectStreamWithTotal(context.getProjectId());

        subjectStreamWithTotal.forEach(subject -> {
            context.headerPut(subject.getName(), 1, 2);
            context.headerMove(Direction.DOWN);
            context.headerPut("得分");
            context.headerMove(Direction.RIGHT);
            context.headerPut("排名");
            context.headerMove(Direction.RIGHT, Direction.UP);
        });
    }

    private void fillData(SheetContext sheetContext) {
        String projectId = sheetContext.getProjectId();

        // 基本信息列
        sheetContext.rowAdd(studentQuery.listStudentInfo(projectId, PROVINCE_RANGE));

        // 全科分数和排名
        sheetContext.rowAdd(getProjectScoreAndRank(projectId));

        // 各科分数和排名
        subjectService.listSubjects(projectId).forEach(subject -> {

            List<Row> rows = scoreQuery.listStudentScore(
                    projectId, PROVINCE_RANGE, Target.subject(subject.getId()));

            String scoreColumnName = "score_" + subject.getId();
            String rankColumnName = "rank_" + subject.getId();
            writeRanks(rows, scoreColumnName, rankColumnName);

            sheetContext.rowAdd(rows);
        });
    }

    //////////////////////////////////////////////////////////////

    private List<Row> getProjectScoreAndRank(String projectId) {
        List<Row> rows = scoreQuery.listStudentScore(projectId,
                PROVINCE_RANGE, Target.project(projectId));

        writeRanks(rows, "score_000", "rank_000");

        return rows;
    }

    private Stream<ExamSubject> getSubjectStreamWithTotal(String projectId) {
        return Stream.concat(
                Stream.of(fakeTotalSubject()),
                subjectService.listSubjects(projectId).stream()
        );
    }

    private ExamSubject fakeTotalSubject() {
        // fullScore 传 0 是因为用不到
        return new ExamSubject("000", "全科", 0);
    }
}
