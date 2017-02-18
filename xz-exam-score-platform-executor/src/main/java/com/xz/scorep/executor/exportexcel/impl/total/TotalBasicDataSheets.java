package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.Row;
import com.xz.ajiaedu.common.excel.ExcelWriter;
import com.xz.ajiaedu.common.lang.Ranker;
import com.xz.scorep.executor.aggritems.ScoreQuery;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.bean.ExamProject;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetHeaderBuilder;
import com.xz.scorep.executor.exportexcel.SheetHeaderBuilder.Direction;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.table.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class TotalBasicDataSheets extends SheetGenerator {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private StudentQuery studentQuery;

    @Autowired
    private ScoreQuery scoreQuery;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        Table table = new Table("student_id");
        ExamProject examProject = sheetContext.getProject();
        ExcelWriter excelWriter = sheetContext.getExcelWriter();

        fillHeader(table, examProject, excelWriter);
        fillData(table, examProject);

        table.sortBy("area", "school_name", "class_name");
        writeTableToSheet(excelWriter, table, 2);
    }

    private void fillData(Table table, ExamProject examProject) {
        String projectId = examProject.getId();

        // 基本信息列
        table.readRows(studentQuery.listStudentInfo(projectId, Range.province("430000")));

        // 全科分数和排名
        table.readRows(getProjectScoreAndRank(projectId));

        // 各科分数和排名
        subjectService.listSubjects(projectId).forEach(subject -> {
            List<Row> rows = scoreQuery.listStudentScore(projectId,
                    Range.province("430000"), Target.subject(subject.getId()));

            fillRank(rows, "score_" + subject.getId(), "rank_" + subject.getId());

            table.readRows(rows);
        });
    }

    private List<Row> getProjectScoreAndRank(String projectId) {
        List<Row> rows = scoreQuery.listStudentScore(projectId,
                Range.province("430000"), Target.project(projectId));

        fillRank(rows, "score_000", "rank_000");

        return rows;
    }

    private void fillRank(List<Row> rows, String scoreColumnName, String rankColumnName) {
        Ranker<String> ranker = new Ranker<>();  // ranker 输出的名词，0 表示第一名
        rows.forEach(row -> ranker.put(row.getString("student_id"), row.getDouble(scoreColumnName, 0)));
        rows.forEach(row -> row.put(rankColumnName, ranker.getRank(row.getString("student_id"), false) + 1));
    }

    private void fillHeader(Table table, ExamProject examProject, ExcelWriter excelWriter) {

        // 从 0 开始依次设置表头字段名称
        String[] columnNames = {"exam_no", "school_exam_no", "student_name", "area", "school_name", "class_name"};
        table.setColumnNames(0, columnNames);

        String projectId = examProject.getId();
        SheetHeaderBuilder builder = new SheetHeaderBuilder(excelWriter);

        // 考生基本信息表头
        Stream.of("考号", "学校考号", "姓名", "市区", "学校", "班级")
                .forEach(text -> builder.setAndMove(text, 2, 1, Direction.RIGHT));

        // 科目列表表头：科目列表前面加个全科
        Stream.concat(
                Stream.of(new ExamSubject("000", "全科", 0)),  // fullScore 用不到
                subjectService.listSubjects(projectId).stream()
        ).forEach(subject -> {

            int currentColumnIndex = builder.getPosition().getColumnIndex();
            table.setColumnIndex("score_" + subject.getId(), currentColumnIndex);
            table.setColumnIndex("rank_" + subject.getId(), currentColumnIndex + 1);

            builder.setAndMove(subject.getName(), 1, 2, Direction.DOWN);
            builder.setAndMove("得分", Direction.RIGHT);
            builder.setAndMove("排名", Direction.RIGHT, Direction.UP);
        });
    }
}
