package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggritems.StudentQuery;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ReportCacheInitializer;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.impl.subject.Row2MapHelper;
import com.xz.scorep.executor.exportexcel.impl.subject.SheetContextHelper;
import com.xz.scorep.executor.exportexcel.impl.subject.SubjectSchoolDetailSheet0;
import com.xz.scorep.executor.project.QuestService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Author: luckylo
 * Date : 2017-03-03
 */
public abstract class TotalSchoolDetailSheet extends SheetGenerator {

    private static String QUERY_STUDENT_SCORE = "select student.id as student_id,\n" +
            "{{table}}.score as total_score_{{subjectId}}\n" +
            "from \n" +
            "student,{{table}}\n" +
            "WHERE\n" +
            "student.id = {{table}}.student_id\n" +
            "and student.school_id = '{{schoolId}}'";

    @Autowired
    StudentQuery studentQuery;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    @Autowired
    ReportCacheInitializer reportCache;

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

    //单科成绩Sheet
    protected void generateEachSubjectSheet(SheetContext sheetContext, ReportCacheInitializer reportCache) {
        SubjectSchoolDetailSheet0.generateSheet0(sheetContext, studentQuery, questService, reportCache);
    }

    private void totalScoreRank(DAO dao, SheetContext sheetContext, AtomicInteger colIndex) {
        String projectId = sheetContext.getProjectId();
        String subjectId = getSubjectId(sheetContext);
        String schoolId = getSchoolId(sheetContext);

        fillSubjectTableHeader("总分", sheetContext, subjectId, colIndex);

        //查询学生成绩
        String totalSql = QUERY_STUDENT_SCORE
                .replace("{{table}}", "score_project")
                .replace("{{subjectId}}", subjectId)
                .replace("{{schoolId}}", schoolId);
        List<Row> rows = dao.query(totalSql);
        sheetContext.rowAdd(Row2MapHelper.row2Map(rows));

        List<String> studentList = rows.stream()
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());

        sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.queryProvinceRank(projectId, studentList, subjectId)));
        sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.querySchoolRank(projectId, studentList, subjectId)));
        sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.queryClassRank(projectId, studentList, subjectId)));

        List<Row> subjects = dao.query("select id ,name ,card_id from subject ");
        for (Row row : subjects) {
            String rowId = row.getString("id");
            String rowName = row.getString("name");

            fillSubjectTableHeader(rowName, sheetContext, rowId, colIndex);

            String eachSql = QUERY_STUDENT_SCORE
                    .replace("{{table}}", "score_subject_" + rowId)
                    .replace("{{subjectId}}", rowId)
                    .replace("{{schoolId}}", schoolId);

            sheetContext.rowAdd(Row2MapHelper.row2Map(dao.query(eachSql)));

            sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.queryProvinceRank(projectId, studentList, rowId)));
            sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.querySchoolRank(projectId, studentList, rowId)));
            sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.queryClassRank(projectId, studentList, rowId)));
        }
    }

    public static void fillSubjectTableHeader(String cellName, SheetContext sheetContext, String subjectId, AtomicInteger columnIndex) {
        sheetContext.headerPut(cellName, 1, 4);
        sheetContext.headerMove(Direction.DOWN);

        String text = subjectId.equals("000") ? "总得分" : "得分";
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
