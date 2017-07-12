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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-10.
 */
public abstract class TotalScoreRankSheet extends SheetGenerator {
    private static String QUERY_STUDENT_SCORE = "select student.id as student_id,\n" +
            "{{table}}.score as total_score_{{subjectId}}\n" +
            "from \n" +
            "student,{{table}}\n" +
            "WHERE\n" +
            "student.id = {{table}}.student_id";
    @Autowired
    StudentQuery studentQuery;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    @Autowired
    ReportCacheInitializer reportCache;


    //联考总分,科目得分排名
    protected void generateTotalScoreRankSheet(SheetContext sheetContext) {
        String projectId = sheetContext.getProjectId();
        sheetContext.tableSetKey("student_id");
        SheetContextHelper.fillStudentBasicInfo(sheetContext, studentQuery);
        AtomicInteger colIndex = new AtomicInteger(5);
        DAO dao = daoFactory.getProjectDao(projectId);
        totalScoreRank(dao, sheetContext, colIndex);
        //按总排名升序排
        sheetContext.rowSortBy("province_rank_000");

        sheetContext.freeze(2, 5);
        sheetContext.saveData();
    }


    //联考单科成绩得分明细
    protected void generateEachSubjectSheet(SheetContext sheetContext) {
        SubjectSchoolDetailSheet0.generateSheet0(sheetContext, studentQuery, questService, reportCache);
    }

    private void totalScoreRank(DAO dao, SheetContext sheetContext, AtomicInteger colIndex) {
        String projectId = sheetContext.getProjectId();
        String subjectId = getSubjectId(sheetContext);

        TotalSchoolDetailSheet.fillSubjectTableHeader("总分", sheetContext, subjectId, colIndex);
        //查询学生成绩
        String totalSql = QUERY_STUDENT_SCORE
                .replace("{{table}}", "score_project")
                .replace("{{subjectId}}", subjectId);

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

            TotalSchoolDetailSheet.fillSubjectTableHeader(rowName, sheetContext, rowId, colIndex);

            String eachSql = QUERY_STUDENT_SCORE
                    .replace("{{table}}", "score_subject_" + rowId)
                    .replace("{{subjectId}}", rowId);

            sheetContext.rowAdd(Row2MapHelper.row2Map(dao.query(eachSql)));

            sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.queryProvinceRank(projectId, studentList, rowId)));
            sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.querySchoolRank(projectId, studentList, rowId)));
            sheetContext.rowAdd(Row2MapHelper.row2Map(reportCache.queryClassRank(projectId, studentList, rowId)));
        }
    }

    protected abstract String getSubjectId(SheetContext sheetContext);

    protected abstract String getSchoolId(SheetContext sheetContext);

    protected abstract String getSubjectName(SheetContext sheetContext);
}
