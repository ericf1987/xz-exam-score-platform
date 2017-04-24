package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelCellStyles;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.SheetTask;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.Direction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-08
 * 平均分及三率统计（班级）
 */
@Component
public class TotalSchoolSubjectAverageSheet extends SheetGenerator {

    public static final String CLASS_MIN_SCORE = "select student.class_id, min(score) as min_score\n" +
            "  from {{scoreTable}} s, student\n" +
            "  where s.student_id=student.id and s.score>0" +
            "  and student.school_id=? " +
            "  group by student.class_id";

    public static final String SCHOOL_MIN_SCORE = "select min(score) as min_score\n" +
            "  from {{scoreTable}} s, student\n" +
            "  where s.student_id=student.id and s.score>0\n" +
            "  and student.school_id=?";

    private static final String QUERY_CLASS_ROWS = "select\n" +
            "a.subject,a.full_score,a.school_name,\n" +
            "a.class_id,a.class_name,a.count,a.average_score,\n" +
            "a.subjective_avg_score,a.objective_avg_score,\n" +
            "a.max_score,\n" +
            "IFNULL(xlnt.xlnt_count,0) as xlnt_count,\n" +
            "concat(IFNULL(xlnt.xlnt_rate,0),'%') as xlnt_rate,\n" +
            "IFNULL(good.good_count,0) as good_count,\n" +
            "CONCAT(IFNULL(good.good_rate,0),'%') as good_rate,\n" +
            "IFNULL(pass.pass_count,0) as pass_count,\n" +
            "CONCAT(IFNULL(pass.pass_rate,0),'%') as pass_rate,\n" +
            "IFNULL(fail.fail_count,0) as fail_count,\n" +
            "CONCAT(IFNULL(fail.fail_rate,0),'%') as fail_rate\n" +
            "from \n" +
            "(\n" +
            "select \n" +
            "'{{subjectName}}' as subject ,\n" +
            "'{{fullScore}}' as full_score,\n" +
            "school.name as school_name,\n" +
            "class.id as class_id,\n" +
            "class.name as class_name,\n" +
            "COUNT(student.id) as count,\n" +
            "convert(format(avg(score_subject_{{subjectId}}.score),2),signed) as average_score,\n" +
            "convert(format(AVG(score_subjective_{{subjectId}}.score),2),signed) as subjective_avg_score,\n" +
            "convert(format(AVG(score_objective_{{subjectId}}.score),2),signed) as objective_avg_score,\n" +
            "max(score_subject_{{subjectId}}.score) as max_score\n" +
            "from school,score_subject_{{subjectId}},class,student,\n" +
            "score_subjective_{{subjectId}},score_objective_{{subjectId}}\n" +
            "where\n" +
            "class.school_id = school.id\n" +
            "and student.class_id = class.id\n" +
            "and student.id = score_subject_{{subjectId}}.student_id\n" +
            "and student.id = score_subjective_{{subjectId}}.student_id\n" +
            "and student.id = score_objective_{{subjectId}}.student_id\n" +
            "and school.id = '{{schoolId}}'\n" +
            "GROUP BY class.id\n" +
            ") a\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_count as xlnt_count,\n" +
            "scorelevelmap.student_rate as xlnt_rate\n" +
            "from school,scorelevelmap,class\n" +
            "where\n" +
            "class.school_id = school.id \n" +
            "and class.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'XLNT'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") xlnt on xlnt.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_count as good_count,\n" +
            "scorelevelmap.student_rate as good_rate\n" +
            "from school,scorelevelmap,class\n" +
            "where\n" +
            "class.school_id = school.id \n" +
            "and class.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'GOOD'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") good on good.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_count as pass_count,\n" +
            "scorelevelmap.student_rate as pass_rate\n" +
            "from school,scorelevelmap,class\n" +
            "where\n" +
            "class.school_id = school.id \n" +
            "and class.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'PASS'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") pass on pass.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "class.id as class_id,\n" +
            "scorelevelmap.student_count as fail_count,\n" +
            "scorelevelmap.student_rate as fail_rate\n" +
            "from school,scorelevelmap,class\n" +
            "where\n" +
            "class.school_id = school.id \n" +
            "and class.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'FAIL'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") fail on fail.class_id = a.class_id\n";

    private static final String QUERY_TOTAL_ROW = "select\n" +
            "a.subject,a.full_score,a.school_name,\n" +
            "a.class_id,a.class_name,a.count,a.average_score,\n" +
            "a.subjective_avg_score,a.objective_avg_score,\n" +
            "a.max_score,\n" +
            "IFNULL(xlnt.xlnt_count,0) as xlnt_count,\n" +
            "concat(IFNULL(xlnt.xlnt_rate,0),'%') as xlnt_rate,\n" +
            "IFNULL(good.good_count,0) as good_count,\n" +
            "CONCAT(IFNULL(good.good_rate,0),'%') as good_rate,\n" +
            "IFNULL(pass.pass_count,0) as pass_count,\n" +
            "CONCAT(IFNULL(pass.pass_rate,0),'%') as pass_rate,\n" +
            "IFNULL(fail.fail_count,0) as fail_count,\n" +
            "CONCAT(IFNULL(fail.fail_rate,0),'%') as fail_rate\n" +
            "from \n" +
            "(\n" +
            "select \n" +
            "'{{subjectName}}' as subject ,\n" +
            "'{{fullScore}}' as full_score,\n" +
            "school.name as school_name,\n" +
            "'total' as class_id,\n" +
            "'全体' as class_name,\n" +
            "COUNT(student.id) as count,\n" +
            "convert(format(avg(score_subject_{{subjectId}}.score),2),signed) as average_score,\n" +
            "convert(format(AVG(score_subjective_{{subjectId}}.score),2),signed) as subjective_avg_score,\n" +
            "convert(format(AVG(score_objective_{{subjectId}}.score),2),signed) as objective_avg_score,\n" +
            "max(score_subject_{{subjectId}}.score) as max_score\n" +
            "from school,score_subject_{{subjectId}},student,\n" +
            "score_subjective_{{subjectId}},score_objective_{{subjectId}}\n" +
            "where\n" +
            "student.school_id = school.id\n" +
            "and student.id = score_subject_{{subjectId}}.student_id\n" +
            "and student.id = score_subjective_{{subjectId}}.student_id\n" +
            "and student.id = score_objective_{{subjectId}}.student_id\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") a\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as xlnt_count,\n" +
            "scorelevelmap.student_rate as xlnt_rate\n" +
            "from school,scorelevelmap\n" +
            "where \n" +
            "school.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'XLNT'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") xlnt on xlnt.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as good_count,\n" +
            "scorelevelmap.student_rate as good_rate\n" +
            "from school,scorelevelmap\n" +
            "where\n" +
            "school.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'GOOD'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") good on good.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as pass_count,\n" +
            "scorelevelmap.student_rate as pass_rate\n" +
            "from school,scorelevelmap\n" +
            "where\n" +
            "school.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'PASS'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") pass on pass.class_id = a.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as fail_count,\n" +
            "scorelevelmap.student_rate as fail_rate\n" +
            "from school,scorelevelmap\n" +
            "where \n" +
            "school.id = scorelevelmap.range_id\n" +
            "and scorelevelmap.target_type = 'subject'\n" +
            "and scorelevelmap.score_level = 'FAIL'\n" +
            "and scorelevelmap.target_id = '{{subjectId}}'\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") fail on fail.class_id = a.class_id";

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {

        createTableHeader(sheetContext);

        String projectId = sheetContext.getProjectId();
        SheetTask task = sheetContext.getSheetTask();

        String schoolId = task.getRange().getId();
        String subjectId = String.valueOf(task.getTarget().getId());
        String subjectName = task.getTarget().getName();
        String scoreTable = "score_subject_" + subjectId;

        ExamSubject examSubject = subjectService.findSubject(projectId, subjectId);
        double fullScore = examSubject.getFullScore();

        DAO dao = daoFactory.getProjectDao(projectId);

        String sql = QUERY_CLASS_ROWS
                .replace("{{subjectName}}",subjectName)
                .replace("{{fullScore}}", StringUtils.removeEnd(String.valueOf(fullScore),".0"))
                .replace("{{subjectId}}",subjectId)
                .replace("{{schoolId}}",schoolId);
        List<Row> rows = dao.query(sql);
        sheetContext.rowAdd(rows);
        sheetContext.rowSortBy("class_name");

        sheetContext.rowAdd(dao.query(CLASS_MIN_SCORE.replace("{{scoreTable}}", scoreTable), schoolId));

        String totalSql = QUERY_TOTAL_ROW
                .replace("{{subjectName}}",subjectName)
                .replace("{{fullScore}}",StringUtils.removeEnd(String.valueOf(fullScore),".0"))
                .replace("{{subjectId}}",subjectId)
                .replace("{{schoolId}}",schoolId);
        sheetContext.rowAdd(dao.queryFirst(totalSql));

        sheetContext.tablePutValue("total", "min_score",
                dao.queryFirst(SCHOOL_MIN_SCORE.replace("{{scoreTable}}", scoreTable), schoolId)
                        .getDouble("min_score", 0));

        sheetContext.rowStyle("total", ExcelCellStyles.Green.name());
        sheetContext.freeze(3, 3);
        sheetContext.saveData();
    }

    private void createTableHeader(SheetContext sheetContext) {
        SheetTask sheetTask = sheetContext.getSheetTask();
        Target target = sheetTask.getTarget();
        sheetContext.tableSetKey("class_id");
        sheetContext.headerPut("平均分及三率统计分析报表(" + target.getName() + ")", 1, 18);

        //公共表头部分
        TotalSchoolAverageSheet.commonTableHeader(sheetContext);

        sheetContext.headerPut("主观题\r\n平均分", 2, 1);
        sheetContext.columnSet(6, "subjective_avg_score");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("客观题\r\n平均分", 2, 1);
        sheetContext.columnSet(7, "objective_avg_score");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("最高分", 2, 1);
        sheetContext.columnSet(8, "max_score");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("最低分", 2, 1);
        sheetContext.columnSet(9, "min_score");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("优秀", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(10, "xlnt_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(11, "xlnt_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);


        sheetContext.headerPut("良好", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(12, "good_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(13, "good_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);


        sheetContext.headerPut("及格", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(14, "pass_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(15, "pass_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);


        sheetContext.headerPut("不及格", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(16, "fail_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(17, "fail_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);
    }
}
