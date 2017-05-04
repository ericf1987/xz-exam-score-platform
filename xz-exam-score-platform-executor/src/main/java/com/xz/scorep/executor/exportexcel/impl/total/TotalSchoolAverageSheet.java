package com.xz.scorep.executor.exportexcel.impl.total;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelCellStyles;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.exportexcel.impl.subject.Row2MapHelper;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.utils.Direction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.xz.scorep.executor.exportexcel.impl.total.TotalSchoolSubjectAverageSheet.CLASS_MIN_SCORE;
import static com.xz.scorep.executor.exportexcel.impl.total.TotalSchoolSubjectAverageSheet.SCHOOL_MIN_SCORE;

/**
 * Author: luckylo
 * Date : 2017-03-06
 * 平均分及三率统计（总分）
 */
@Component
public class TotalSchoolAverageSheet extends SheetGenerator {

    public static String QUERY_CLASS_BASE_INFO = "select \n" +
            "a.subject,a.full_score,\n" +
            "a.class_id,a.school_name,\n" +
            "a.class_name,a.count,\n" +
            "a.average_score,a.max_score,\n" +
            "IFNULL(xlnt.xlnt_count,0) as xlnt_count,\n" +
            "concat(IFNULL(xlnt.xlnt_rate,0),'%') as xlnt_rate,\n" +
            "IFNULL(good.good_count,0) as good_count,\n" +
            "concat(IFNULL(good.good_rate,0),'%') as good_rate,\n" +
            "IFNULL(pass.pass_count,0) as pass_count,\n" +
            "concat(IFNULL(pass.pass_rate,0),'%') as pass_rate,\n" +
            "IFNULL(fail.fail_count,0) as fail_count,\n" +
            "concat(IFNULL(fail.fail_rate,0),'%') as fail_rate,\n" +
            "total.all_pass_count\n," +
            "total.all_pass_rate\n," +
            "total.all_fail_count\n," +
            "total.all_fail_rate\n" +
            "from (\n" +
            "select '总分' as subject,\n" +
            "'{{fullScore}}' as full_score,\n" +
            "class.id as class_id,\n" +
            "school.name as school_name,\n" +
            "class.name as class_name,\n" +
            "COUNT(student.id) as count,\n" +
            "convert(FORMAT(AVG(score_project.score),2),decimal(10,2)) as average_score,\n" +
            "MAX(score_project.score) as max_score\n" +
            "from school,class,\n" +
            "student,score_project\n" +
            "where \n" +
            "student.class_id = class.id\n" +
            "and class.school_id = school.id\n" +
            "and student.school_id = school.id\n" +
            "and student.id = score_project.student_id\n" +
            "and school.id = '{{schoolId}}'\n" +
            "GROUP BY class.id\n" +
            ") a\n" +
            "LEFT JOIN(\n" +
            "select class.id as class_id,\n" +
            "scorelevelmap.student_count as xlnt_count,\n" +
            "scorelevelmap.student_rate as xlnt_rate\n" +
            "from class,scorelevelmap\n" +
            "where \n" +
            "scorelevelmap.range_type = 'class'\n" +
            "and scorelevelmap.range_id = class.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.score_level='XLNT'\n" +
            ") xlnt on a.class_id = xlnt.class_id\n" +
            "LEFT JOIN(\n" +
            "select class.id as class_id,\n" +
            "scorelevelmap.student_count as good_count,\n" +
            "scorelevelmap.student_rate as good_rate\n" +
            "from class,scorelevelmap\n" +
            "where \n" +
            "scorelevelmap.range_type = 'class'\n" +
            "and scorelevelmap.range_id = class.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.score_level='GOOD'\n" +
            ") good on good.class_id = a.class_id\n" +
            "LEFT JOIN(\n" +
            "select class.id as class_id,\n" +
            "scorelevelmap.student_count as pass_count,\n" +
            "scorelevelmap.student_rate as pass_rate\n" +
            "from class,scorelevelmap\n" +
            "where \n" +
            "scorelevelmap.range_type = 'class'\n" +
            "and scorelevelmap.range_id = class.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.score_level='PASS'\n" +
            ") pass on pass.class_id = a.class_id\n" +
            "LEFT JOIN(\n" +
            "select class.id as class_id,\n" +
            "scorelevelmap.student_count as fail_count,\n" +
            "scorelevelmap.student_rate as fail_rate\n" +
            "from class,scorelevelmap\n" +
            "where \n" +
            "scorelevelmap.range_type = 'class'\n" +
            "and scorelevelmap.range_id = class.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and class.school_id = '{{schoolId}}'\n" +
            "and scorelevelmap.score_level='FAIL'\n" +
            ") fail on fail.class_id = a.class_id\n" +
            "LEFT JOIN(\n" +
            "select DISTINCT range_id as class_id ,all_pass_count,\n" +
            "concat(all_pass_rate,'%') as all_pass_rate,all_fail_count,\n" +
            "concat(all_fail_rate,'%') as all_fail_rate from all_pass_or_fail,student\n" +
            " where student.class_id = all_pass_or_fail.range_id\n" +
            "and student.school_id = '{{schoolId}}'" +
            ") total on total.class_id = a.class_id";


    public static final String QUERY_TOTAL_INFO = "select\n" +
            "a.subject,a.full_score,a.class_id,\n" +
            "a.school_name,a.class_name,a.count,\n" +
            "a.average_score,a.max_score,\n" +
            "IFNULL(xlnt.xlnt_count,0) as xlnt_count,\n" +
            "concat(IFNULL(xlnt.xlnt_rate,0),'%') as xlnt_rate,\n" +
            "IFNULL(good.good_count,0) as good_count,\n" +
            "concat(IFNULL(good.good_rate,0),'%') as good_rate,\n" +
            "IFNULL(pass.pass_count,0) as pass_count,\n" +
            "concat(IFNULL(pass.pass_rate,0),'%') as pass_rate,\n" +
            "IFNULL(fail.fail_count,0) as fail_count,\n" +
            "concat(IFNULL(fail.fail_rate,0),'%') as fail_rate,\n" +
            "IFNULL(total.all_pass_count,0) as all_pass_count,\n" +
            "concat(IFNULL(total.all_pass_rate,0),'%') as all_pass_rate,\n" +
            "IFNULL(total.all_fail_count,0) as all_fail_count,\n" +
            "concat(IFNULL(total.all_fail_rate,0),'%') as all_fail_rate\n" +
            "FROM\n" +
            "(\n" +
            "select \n" +
            "'总分' as subject, '{{fullScore}}' as full_score,\n" +
            "'total' as class_id,school.name as school_name,\n" +
            "'全体' as class_name,COUNT(student.id) as count,\n" +
            "convert(FORMAT(AVG(score_project.score),2),decimal(10,2)) as average_score,\n" +
            "MAX(score_project.score) as max_score\n" +
            "from school,student,score_project\n" +
            "where \n" +
            "student.school_id = school.id\n" +
            "and student.id = score_project.student_id\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") a\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as xlnt_count,\n" +
            "scorelevelmap.student_rate as xlnt_rate\n" +
            "from school,scorelevelmap\n" +
            "WHERE\n" +
            "scorelevelmap.score_level = 'XLNT'\n" +
            "AND scorelevelmap.range_id = school.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and scorelevelmap.range_type = 'School'\n" +
            "and scorelevelmap.range_id = '{{schoolId}}'\n" +
            ") xlnt on a.class_id = xlnt.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as good_count,\n" +
            "scorelevelmap.student_rate as good_rate\n" +
            "from school,scorelevelmap\n" +
            "WHERE\n" +
            "scorelevelmap.score_level = 'GOOD'\n" +
            "AND scorelevelmap.range_id = school.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and scorelevelmap.range_type = 'School'\n" +
            "and scorelevelmap.range_id = '{{schoolId}}'\n" +
            ") good on a.class_id = good.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as pass_count,\n" +
            "scorelevelmap.student_rate as pass_rate\n" +
            "from school,scorelevelmap\n" +
            "WHERE\n" +
            "scorelevelmap.score_level = 'PASS'\n" +
            "AND scorelevelmap.range_id = school.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and scorelevelmap.range_type = 'School'\n" +
            "and scorelevelmap.range_id = '{{schoolId}}'\n" +
            ") pass on a.class_id = pass.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "scorelevelmap.student_count as fail_count,\n" +
            "scorelevelmap.student_rate as fail_rate\n" +
            "from school,scorelevelmap\n" +
            "WHERE\n" +
            "scorelevelmap.score_level = 'FAIL'\n" +
            "AND scorelevelmap.range_id = school.id\n" +
            "and scorelevelmap.target_type ='Project'\n" +
            "and scorelevelmap.range_type = 'School'\n" +
            "and scorelevelmap.range_id = '{{schoolId}}'\n" +
            ") fail on a.class_id = fail.class_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select \n" +
            "'total' as class_id,\n" +
            "all_pass_or_fail.all_pass_count as all_pass_count,\n" +
            "all_pass_or_fail.all_pass_rate as all_pass_rate,\n" +
            "all_pass_or_fail.all_fail_count as all_fail_count,\n" +
            "all_pass_or_fail.all_fail_rate as all_fail_rate\n" +
            "from school,all_pass_or_fail\n" +
            "where \n" +
            "school.id = all_pass_or_fail.range_id\n" +
            "and school.id = '{{schoolId}}'\n" +
            ") total on total.class_id = a.class_id";

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ProjectService projectService;


    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        String projectId = sheetContext.getProjectId();
        sheetContext.tableSetKey("class_id");

        sheetContext.headerPut("平均分及三率统计分析报表（总分）", 1, 20);
        fillTableHeader(sheetContext);

        String schoolId = sheetContext.getSheetTask().getRange().getId();

        String fullScore = String.valueOf(projectService.findProject(projectId).getFullScore());

        //平均分、最高分、最低分、优率、良率、及格率、不及格率、全科及格率、全科不及格率
        String classSql = QUERY_CLASS_BASE_INFO
                .replace("{{fullScore}}", StringUtils.removeEnd(fullScore,".0"))
                .replace("{{schoolId}}", schoolId);
        DAO dao = daoFactory.getProjectDao(projectId);
        List<Row> rows = dao.query(classSql);
        sheetContext.rowAdd(Row2MapHelper.row2Map(rows));

        String classMinScoreSql = CLASS_MIN_SCORE.replace("{{scoreTable}}", "score_project");
        sheetContext.rowAdd(Row2MapHelper.row2Map(dao.query(classMinScoreSql, schoolId)));

        sheetContext.rowSortBy("class_name");

        //总计栏
        String totalSql = QUERY_TOTAL_INFO
                .replace("{{schoolId}}", schoolId)
                .replace("{{fullScore}}", StringUtils.removeEnd(fullScore,".0"));
        sheetContext.rowAdd(dao.queryFirst(totalSql));

        sheetContext.tablePutValue("total", "min_score",
                dao.queryFirst(SCHOOL_MIN_SCORE.replace("{{scoreTable}}", "score_project"), schoolId)
                        .getDouble("min_score", 0));

        sheetContext.rowStyle("total", ExcelCellStyles.Green.name());
        sheetContext.freeze(3, 3);
        sheetContext.saveData();
    }


    private void fillTableHeader(SheetContext sheetContext) {

        commonTableHeader(sheetContext);

        sheetContext.headerPut("最高分", 2, 1);
        sheetContext.columnSet(6, "max_score");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("最低分", 2, 1);
        sheetContext.columnSet(7, "min_score");
        sheetContext.headerMove(Direction.RIGHT);


        sheetContext.headerPut("优秀", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(8, "xlnt_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(9, "xlnt_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);


        sheetContext.headerPut("良好", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(10, "good_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(11, "good_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);


        sheetContext.headerPut("及格", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(12, "pass_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(13, "pass_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);


        sheetContext.headerPut("不及格", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(14, "fail_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(15, "fail_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);

        sheetContext.headerPut("全科及格率", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(16, "all_pass_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(17, "all_pass_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);

        sheetContext.headerPut("全科不及格率", 1, 2);
        sheetContext.headerMove(Direction.DOWN);
        sheetContext.headerPut("人数", 1, 1);
        sheetContext.columnSet(18, "all_fail_count");
        sheetContext.headerMove(Direction.RIGHT);
        sheetContext.headerPut("比率", 1, 1);
        sheetContext.columnSet(19, "all_fail_rate");
        sheetContext.headerMove(Direction.RIGHT, Direction.UP);
    }

    //学校平均分三率  和科目平均分三率公共表头
    public static void commonTableHeader(SheetContext sheetContext) {
        sheetContext.headerMove(Direction.DOWN);

        sheetContext.headerPut("科目", 2, 1);
        sheetContext.columnSet(0, "subject");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("满分", 2, 1);
        sheetContext.columnSet(1, "full_score");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("学校", 2, 1);
        sheetContext.columnWidth(2, 20);
        sheetContext.columnSet(2, "school_name");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("班级", 2, 1);
        sheetContext.columnSet(3, "class_name");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("总人数", 2, 1);
        sheetContext.columnSet(4, "count");
        sheetContext.headerMove(Direction.RIGHT);

        sheetContext.headerPut("平均分", 2, 1);
        sheetContext.columnSet(5, "average_score");
        sheetContext.headerMove(Direction.RIGHT);
    }

}
