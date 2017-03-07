package com.xz.scorep.executor.exportexcel.impl.total;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-03-06
 */
@Component
public class TotalSchoolAverageSheet extends SheetGenerator {

    public static String QUERY_CLASS_BASE_INFO = "select \n" +
            "a.subject,a.full_score,\n" +
            "a.class_id,a.school_name,\n" +
            "a.class_name,a.count,\n" +
            "a.average_score,a.max_score,\n" +
            "a.min_score,\n" +
            "IFNULL(xlnt.xlnt_count,0) as xlnt_count,\n" +
            "IFNULL(xlnt.xlnt_rate,0) as xlnt_rate,\n" +
            "IFNULL(good.good_count,0) as good_count,\n" +
            "IFNULL(good.good_rate,0) as good_rate,\n" +
            "IFNULL(pass.pass_count,0) as pass_count,\n" +
            "IFNULL(pass.pass_rate,0) as pass_rate,\n" +
            "IFNULL(fail.fail_count,0) as fail_count,\n" +
            "IFNULL(fail.fail_rate,0) as fail_rate\n" +
            "from (\n" +
            "select '总分' as subject,\n" +
            "'{{fullScore}}' as full_score,\n" +
            "class.id as class_id,\n" +
            "school.name as school_name,\n" +
            "class.name as class_name,\n" +
            "COUNT(student.id) as count,\n" +
            "FORMAT(AVG(score_project.score),2) as average_score,\n" +
            "MAX(score_project.score) as max_score,\n" +
            "MIN(score_project.score) as min_score\n" +
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
            ") fail on fail.class_id = a.class_id\n";

    public static final String QUERY_CLASS_PASS_FAIL = "select DISTINCT range_id as class_id ,all_pass_count," +
            " concat(all_pass_rate,'%') as all_pass_rate,all_fail_count," +
            " concat(all_fail_rate,'%') as all_fail_rate from all_pass_or_fail,student\n" +
            " where student.class_id = all_pass_or_fail.range_id\n" +
            " and student.school_id = '{{schoolId}}'";

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ReportConfigService reportConfigService;

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

        //平均分、最高分、最低分、优率、良率、及格率、不及格率
        String sql = QUERY_CLASS_BASE_INFO
                .replace("{{fullScore}}", fullScore)
                .replace("{{schoolId}}", schoolId);
        DAO dao = daoFactory.getProjectDao(projectId);
        List<Row> rows = dao.query(sql);
        sheetContext.rowAdd(rows);
        sheetContext.rowAdd(dao.query(QUERY_CLASS_PASS_FAIL.replace("{{schoolId}}", schoolId)));
        sheetContext.rowSortBy("class_name");

        Row row = createTotalRow();
        sheetContext.rowAdd(row);

        sheetContext.freeze(3, 3);
        sheetContext.saveData();
    }

    private Row createTotalRow() {
        Row row = new Row();
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        row.put("","");
        return row;
    }

    private void fillTableHeader(SheetContext sheetContext) {
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

}
