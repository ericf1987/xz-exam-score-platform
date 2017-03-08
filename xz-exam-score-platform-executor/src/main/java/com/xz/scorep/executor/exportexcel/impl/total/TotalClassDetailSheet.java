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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: luckylo
 * Date : 2017-03-06
 * 班级分数排名、得分明细表(全科)
 */
public abstract class TotalClassDetailSheet extends SheetGenerator {


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
            "rank_province,\n" +
            "class \n" +
            "where \n" +
            "student.id = {{table}}.student_id \n" +
            "and student.id = rank_class.student_id\n" +
            "and rank_class.subject_id = '{{subjectId}}'\n" +
            "and student.id = rank_school.student_id \n" +
            "and rank_school.subject_id = '{{subjectId}}'\n" +
            "and student.id = rank_province.student_id\n" +
            "and rank_province.subject_id = '{{subjectId}}'\n" +
            "and student.school_id = class.school_id\n" +
            "and student.class_id = '{{classId}}'";


    @Autowired
    DAOFactory daoFactory;

    @Autowired
    StudentQuery studentQuery;

    @Autowired
    QuestService questService;


    protected void generateTotalSheet(SheetContext sheetContext) {
        sheetContext.tableSetKey("student_id");
        //学生基本信息
        SheetContextHelper.fillStudentBasicInfo(sheetContext, studentQuery);

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);

        AtomicInteger colIndex = new AtomicInteger(5);

        totalScoreRank(dao, sheetContext, colIndex);
        //按总排名升序排
        sheetContext.rowSortBy("province_rank_000");

        sheetContext.freeze(2, 5);
        sheetContext.saveData();
    }

    private void totalScoreRank(DAO dao, SheetContext sheetContext, AtomicInteger colIndex) {
        String subjectId = getSubjectId(sheetContext);
        String classId = getClassId(sheetContext);
        TotalSchoolDetailSheet.fillSubjectTableHeader("总分", sheetContext, subjectId, colIndex);
        //学生总成绩排名...
        String totalSql = QUERY_STUDENT_RANK
                .replace("{{table}}", "score_project")
                .replace("{{subjectId}}", subjectId)
                .replace("{{classId}}",classId);
        List<Row> rows = dao.query(totalSql);
        sheetContext.rowAdd(rows);

        List<Row> subjects = dao.query("select id ,name ,card_id from subject ");
        for (Row row : subjects) {
            String rowId = row.getString("id");
            String rowName = row.getString("name");

            TotalSchoolDetailSheet.fillSubjectTableHeader(rowName, sheetContext, rowId, colIndex);
            //学生单科成绩排名...
            String eachSql = QUERY_STUDENT_RANK
                    .replace("{{table}}", "score_subject_" + rowId)
                    .replace("{{subjectId}}", rowId)
                    .replace("{{classId}}", classId);

            sheetContext.rowAdd(dao.query(eachSql));
        }
    }


    protected void generateEachSubjectSheet(SheetContext sheetContext) {
        SubjectSchoolDetailSheet0.generateSheet0(sheetContext, studentQuery, questService);
    }

    protected abstract String getSubjectId(SheetContext sheetContext);

    protected abstract String getClassId(SheetContext sheetContext);
}
