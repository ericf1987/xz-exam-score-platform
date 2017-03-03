package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.impl.subject.SheetContextHelper;
import com.xz.scorep.executor.exportexcel.impl.subject.SubjectSchoolDetailSheet0;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: luckylo
 * Date : 2017-03-03
 */
public abstract class TotalSchoolDetailSheet extends SheetGenerator {

    public static String QUERY_STUDENT_RANK = "select \n" +
            "student.id as student_id," +
            "{{table}}.score as total_score_{{subjectId}},\n" +
            "rank_class.rank as class_rank_{{subjectId}},\n" +
            "rank_school.rank as school_rank_{{subjectId}},\n" +
            "rank_province.rank as province_rank_{{subjectId}}\n" +
            "from \n" +
            "student,\n" +
            "{{table}},\n" +
            "rank_class,\n" +
            "rank_school,\n" +
            "rank_province\n" +
            "where \n" +
            "student.id = {{table}}.student_id \n" +
            "and student.id = rank_class.student_id\n" +
            "and rank_class.subject_id = '{{subjectId}}'\n" +
            "and student.id = rank_school.student_id \n" +
            "and rank_school.subject_id = '{{subjectId}}'\n" +
            "and student.id = rank_province.student_id\n" +
            "and rank_province.subject_id = '{{subjectId}}'\n" +
            "and student.school_id ='{{schoolId}}'";

    @Autowired
    StudentQuery studentQuery;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    //总成排名
    protected void generateTotalScoreRankSheet(SheetContext sheetContext) {
        String projectId = sheetContext.getProjectId();

        sheetContext.tableSetKey("student_id");
        //填充学生基本信息
        SheetContextHelper.fillStudentBasicInfo(sheetContext, studentQuery);

        DAO dao = daoFactory.getProjectDao(projectId);

        AtomicInteger colIndex = new AtomicInteger(5);

        totalScoreRank(dao, sheetContext, colIndex);
        //按总排名升序排
        sheetContext.rowSortBy("province_rank_000");

        sheetContext.freeze(2, 5);
        sheetContext.saveData();
    }

    //单科成绩排名
    protected void generateEachSubjectSheet(SheetContext sheetContext) {
        SubjectSchoolDetailSheet0.generateSheet0(sheetContext, studentQuery, questService);
    }

    private void totalScoreRank(DAO dao, SheetContext sheetContext, AtomicInteger colIndex) {
        String subjectId = getSubjectId(sheetContext);
        String schoolId = getSchoolId(sheetContext);

        fillSubjectTableHeader("总分", sheetContext, subjectId, colIndex);

        //学生总成绩排名...
        String totalSql = QUERY_STUDENT_RANK
                .replace("{{table}}", "score_project")
                .replace("{{subjectId}}", subjectId)
                .replace("{{schoolId}}", schoolId);
        List<Row> rows = dao.query(totalSql);
        sheetContext.rowAdd(rows);

        List<Row> subjects = dao.query("select id ,name ,card_id from subject ");
        for (Row row : subjects) {
            String rowId = row.getString("id");
            String rowName = row.getString("name");

            fillSubjectTableHeader(rowName, sheetContext, rowId, colIndex);
            //学生单科成绩排名...
            String eachSql = QUERY_STUDENT_RANK
                    .replace("{{table}}", "score_subject_" + rowId)
                    .replace("{{subjectId}}", rowId)
                    .replace("{{schoolId}}", schoolId);

            sheetContext.rowAdd(dao.query(eachSql));
        }
    }

    private void fillSubjectTableHeader(String cellName, SheetContext sheetContext, String subjectId, AtomicInteger columnIndex) {
        sheetContext.headerPut(cellName, 1, 4);
        sheetContext.headerMove(Direction.DOWN);

        String text = subjectId.endsWith("000") ? "总得分" : "得分";
        sheetContext.headerPut(text, 1, 1);
        sheetContext.columnSet(columnIndex.getAndIncrement(), "total_score_" + subjectId);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("班排名", 1, 1);
        sheetContext.columnSet(columnIndex.getAndIncrement(), "class_rank_" + subjectId);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("校排名", 1, 1);
        sheetContext.columnSet(columnIndex.getAndIncrement(), "school_rank_" + subjectId);
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("总排名", 1, 1);
        sheetContext.columnSet(columnIndex.getAndIncrement(), "province_rank_" + subjectId);
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);
    }

    protected abstract String getSubjectId(SheetContext sheetContext);

    protected abstract String getSchoolId(SheetContext sheetContext);

    protected abstract String getSubjectName(SheetContext sheetContext);
}
