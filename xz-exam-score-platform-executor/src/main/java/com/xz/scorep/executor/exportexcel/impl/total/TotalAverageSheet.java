package com.xz.scorep.executor.exportexcel.impl.total;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.NumberUtil;
import com.xz.ajiaedu.common.lang.Ranker;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportexcel.ExcelCellStyles;
import com.xz.scorep.executor.exportexcel.SheetContext;
import com.xz.scorep.executor.exportexcel.SheetGenerator;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Author: luckylo
 * Date : 2017-03-02
 * 联考学校平均分统计分析
 */

public abstract class TotalAverageSheet extends SheetGenerator {

    private static final String SUBJECT = "score_subject_";

    private static final String PASS_OR_FAIL = "select range_id as school_id,concat(all_pass_rate,'%') as all_pass," +
            " concat(all_fail_rate,'%') as all_fail,all_pass_count,all_fail_count from all_pass_or_fail where range_type = 'school'";

    private static final String SCHOOL_PROJECT_OR_SUBJECT_INFO = "SELECT\n" +
            " a.school_id,a.school_name,a.student_count,a.max_score,\n" +
            " a.min_score,a.average_score,CONCAT(IFNULL(xlnt.xlnt, '0.00'),'%') AS excellent,\n" +
            " CONCAT(IFNULL(good.good, '0.00'),'%') AS good,\n" +
            " CONCAT(IFNULL(pass.pass, '0.00'),'%') AS pass,\n" +
            " CONCAT(IFNULL(fail.fail, '0.00'),'%') AS fail\n" +
            " FROM\n" +
            " (\n" +
            " SELECT\n" +
            " school.id AS school_id,\n" +
            " school. NAME AS school_name,\n" +
            " COUNT(student.id) student_count,\n" +
            " MAX({{table}}.score) AS max_score,\n" +
            " MIN({{table}}.score) AS min_score,\n" +
            " FORMAT(AVG({{table}}.score),2) AS average_score\n" +
            " FROM\n" +
            " school,\n" +
            " student,\n" +
            " {{table}}\n" +
            " WHERE\n" +
            " school.id = student.school_id\n" +
            " AND student.id = {{table}}.student_id\n" +
            " GROUP BY\n" +
            " school.id\n" +
            " ) a\n" +
            " LEFT JOIN (\n" +
            " SELECT\n" +
            " school.id,\n" +
            " scorelevelmap.student_rate AS xlnt\n" +
            " FROM\n" +
            " school,\n" +
            " scorelevelmap\n" +
            " WHERE\n" +
            " scorelevelmap.range_type = 'school'\n" +
            " AND scorelevelmap.target_type = '{{targetType}}'\n" +
            " and scorelevelmap.target_id = '{{targetId}}'\n" +
            " AND scorelevelmap.range_id = school.id\n" +
            " AND scorelevelmap.score_level = 'XLNT'\n" +
            " ) xlnt ON a.school_id = xlnt.id\n" +
            " LEFT JOIN (\n" +
            " SELECT\n" +
            " school.id,\n" +
            " scorelevelmap.student_rate AS good\n" +
            " FROM\n" +
            " school,\n" +
            " scorelevelmap\n" +
            " WHERE\n" +
            " scorelevelmap.range_type = 'school'\n" +
            " AND scorelevelmap.target_type = '{{targetType}}'\n" +
            " and scorelevelmap.target_id = '{{targetId}}'\n" +
            " AND scorelevelmap.range_id = school.id\n" +
            " AND scorelevelmap.score_level = 'GOOD'\n" +
            " ) good ON good.id = a.school_id\n" +
            " LEFT JOIN (\n" +
            " SELECT\n" +
            " school.id,\n" +
            " scorelevelmap.student_rate AS pass\n" +
            " FROM\n" +
            " school,\n" +
            " scorelevelmap\n" +
            " WHERE\n" +
            " scorelevelmap.range_type = 'school'\n" +
            " AND scorelevelmap.target_type = '{{targetType}}'\n" +
            " and scorelevelmap.target_id = '{{targetId}}'\n" +
            " AND scorelevelmap.range_id = school.id\n" +
            " AND scorelevelmap.score_level = 'PASS'\n" +
            " ) pass ON pass.id = a.school_id\n" +
            " LEFT JOIN (\n" +
            " SELECT\n" +
            " school.id,\n" +
            " scorelevelmap.student_rate AS fail\n" +
            " FROM\n" +
            " school,\n" +
            " scorelevelmap\n" +
            " WHERE\n" +
            " scorelevelmap.range_type = 'school'\n" +
            " AND scorelevelmap.target_type = '{{targetType}}'\n" +
            " and scorelevelmap.target_id = '{{targetId}}'\n" +
            " AND scorelevelmap.range_id = school.id\n" +
            " AND scorelevelmap.score_level = 'FAIL'\n" +
            " ) fail ON fail.id = a.school_id";


    private static final String TOTAL_SCORE_INFO = "select \n" +
            "a.school_id,a.school_name,a.student_count,\n" +
            "a.max_score,a.min_score,a.average_score,\n" +
            "a.average_range,\n" +
            "xlnt.excellent,good.good,pass.pass,fail.fail,\n" +
            "pass_fail.all_pass,pass_fail.all_fail\n" +
            "FROM\n" +
            "(\n" +
            "select\n" +
            "'total' as school_id,'总体'as school_name, \n" +
            "COUNT(student.id) as student_count,max(score_project.score) as max_score,\n" +
            "MIN(score_project.score) as min_score,format(AVG(score_project.score),2) as average_score,\n" +
            "'--' as average_range\n" +
            "from student,score_project\n" +
            "where  student.id = score_project.student_id\n" +
            ") a\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id ,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as excellent from scorelevelmap\n" +
            "where scorelevelmap.range_type ='Province'\n" +
            "and scorelevelmap.target_id = '{{projectId}}'\n" +
            "and scorelevelmap.score_level = 'XLNT'\n" +
            ") xlnt on xlnt.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as good from scorelevelmap\n" +
            "where scorelevelmap.range_type ='Province'\n" +
            "and scorelevelmap.target_id = '{{projectId}}'\n" +
            "and scorelevelmap.score_level = 'GOOD'\n" +
            ") good on good.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as pass from scorelevelmap\n" +
            "where scorelevelmap.range_type ='Province'\n" +
            "and scorelevelmap.target_id = '{{projectId}}'\n" +
            "and scorelevelmap.score_level = 'PASS'\n" +
            ") pass on pass.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as fail from scorelevelmap\n" +
            "where scorelevelmap.range_type ='Province'\n" +
            "and scorelevelmap.target_id = '{{projectId}}'\n" +
            "and scorelevelmap.score_level = 'FAIL'\n" +
            ") fail on fail.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,\n" +
            "concat(IFNULL(all_pass_or_fail.all_pass_rate,'0.00'),'%') as all_pass,\n" +
            "concat(IFNULL(all_pass_or_fail.all_fail_rate,'0.00'),'%') as all_fail\n" +
            "from all_pass_or_fail\n" +
            "where \n" +
            "all_pass_or_fail.range_id = '{{projectId}}'\n" +
            ") pass_fail on pass_fail.school_id = a.school_id \n";


    private static final String SUBJECT_TOTAL_ROW = "select \n" +
            "a.school_id,a.school_name,a.student_count,\n" +
            "a.max_score,a.min_score,a.average_score,\n" +
            "a.average_range,xlnt.excellent,good.good,\n" +
            "pass.pass,fail.fail\n" +
            "FROM\n" +
            "(\n" +
            "select \n" +
            "'total' as school_id, '总体' as school_name,COUNT(student.id) as student_count,\n" +
            "max(score_subject_{{subjectId}}.score) as max_score,MIN(score_subject_{{subjectId}}.score) as min_score,\n" +
            "FORMAT(avg(score_subject_{{subjectId}}.score),2) as average_score,'--' as average_range\n" +
            "from student,score_subject_{{subjectId}}\n" +
            "WHERE\n" +
            "student.id = score_subject_{{subjectId}}.student_id\n" +
            ") a \n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as excellent\n" +
            "from scorelevelmap\n" +
            "where\n" +
            "scorelevelmap.range_type ='Province'\n" +
            "AND scorelevelmap.target_id ='{{subjectId}}'\n" +
            "and scorelevelmap.score_level = 'XLNT'\n" +
            ") xlnt on xlnt.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as good\n" +
            "from scorelevelmap\n" +
            "where\n" +
            "scorelevelmap.range_type ='Province'\n" +
            "AND scorelevelmap.target_id ='{{subjectId}}'\n" +
            "and scorelevelmap.score_level = 'GOOD'\n" +
            ") good on good.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as pass\n" +
            "from scorelevelmap\n" +
            "where\n" +
            "scorelevelmap.range_type ='Province'\n" +
            "AND scorelevelmap.target_id ='{{subjectId}}'\n" +
            "and scorelevelmap.score_level = 'PASS'\n" +
            ") pass on pass.school_id = a.school_id\n" +
            "LEFT JOIN\n" +
            "(\n" +
            "select 'total' as school_id,concat(IFNULL(scorelevelmap.student_rate,'0.00'),'%') as fail\n" +
            "from scorelevelmap\n" +
            "where\n" +
            "scorelevelmap.range_type ='Province'\n" +
            "AND scorelevelmap.target_id ='{{subjectId}}'\n" +
            "and scorelevelmap.score_level = 'FAIL'\n" +
            ") fail on fail.school_id = a.school_id\n";

    private static final String SCHOOL_PROJECT_OVER_AVERAGE_RATE = "select student.school_id ,CONCAT(FORMAT((COUNT(student.id)/a.count) *100,2),'%') as over_average from student\n" +
            " LEFT JOIN {{table}} on student.id = {{table}}.student_id\n" +
            " LEFT JOIN (SELECT\n" +
            " school.id  as school_id,\n" +
            " COUNT(student.id) as count,\n" +
            " FORMAT(AVG({{table}}.score),2) AS average_score\n" +
            " FROM\n" +
            " school,\n" +
            " student,\n" +
            " {{table}}\n" +
            " WHERE\n" +
            " school.id = student.school_id\n" +
            " AND student.id = {{table}}.student_id\n" +
            " GROUP BY\n" +
            " school.id) a on a.school_id = student.school_id\n" +
            " where {{table}}.score >= a.average_score GROUP BY student.school_id";

    private static final String TOTAL_OVER_AVERAGE_RATE = "SELECT\n" +
            "@total :=(select COUNT(1) from {{table}}),\n" +
            "@over := (select count(1) from {{table}} where score > {{averageScore}}),\n" +
            "@over /@total as over_average\n";

    private static final String NOTE = "注：三率占比说明：优 {{xlnt}}，良{{good}}，及格{{pass}}";
    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;


    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        generateEachSheet(sheetContext);
    }

    void generateEachSheet(SheetContext sheetContext) {
        Map<String, String> tableHeader = getTableHeader();
        putTableHeader(sheetContext, tableHeader);

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);

        if (tableHeader.get("all_pass") == null) {//单科
            String subjectId = getSubjectId(sheetContext);
            //查每个学校每科的人数、最高分、最低分、平均分、四率
            List<Row> rows = putSchoolSubjectInfo(sheetContext, dao, subjectId);

            //先按平均分排名,最后增加总计行
            accordingAverageSorting(sheetContext, rows);

            //每一科目的总计栏
            Row totalRow = getSchoolSubjectTotalRow(dao, subjectId);
            sheetContext.rowAdd(totalRow);

        } else {//全科
            //查学校参考人数、最高分、最低分、平均分、四率
            List<Row> rows = putSchoolProjectInfo(sheetContext, projectId, dao);

            //先按平均分排名,最后增加总计行
            accordingAverageSorting(sheetContext, rows);

            Row total = getSchoolProjectTotalRow(projectId, dao);
            sheetContext.rowAdd(total);
        }

        sheetContext.rowStyle("total", ExcelCellStyles.Green.name());
        //添加注释
        putNoteRow(sheetContext, projectId);

        sheetContext.freeze(3, 1);
        sheetContext.saveData();// 保存到 ExcelWriter
    }

    private Row getSchoolProjectTotalRow(String projectId, DAO dao) {
        //总计栏
        Row total = dao.queryFirst(TOTAL_SCORE_INFO.replace("{{projectId}}", projectId));

        double averageScore = total.getDouble("average_score", 0);
        String averageRateSql = TOTAL_OVER_AVERAGE_RATE
                .replace("{{table}}", "score_project")
                .replace("{{averageScore}}", String.valueOf(averageScore));
        Row overAverageRate = dao.queryFirst(averageRateSql);

        String overRate = String.format("%.02f%%",
                NumberUtil.scale(100.0 * overAverageRate.getDouble("over_average", 0), 2));
        total.put("over_average", overRate);

        Row passOrFail = dao.queryFirst("select * from all_pass_or_fail where range_type='province'");
        total.put("all_pass", passOrFail.getDouble("all_pass_rate", 0) + "%");
        total.put("all_fail", passOrFail.getDouble("all_fail_rate", 0) + "%");

        return total;
    }

    private List<Row> putSchoolProjectInfo(SheetContext sheetContext, String projectId, DAO dao) {
        String sql = SCHOOL_PROJECT_OR_SUBJECT_INFO
                .replace("{{table}}", "score_project")
                .replace("{{targetType}}", getTargetType(sheetContext))
                .replace("{{targetId}}", projectId);

        List<Row> rows = dao.query(sql);
        sheetContext.rowAdd(rows);

        //超均率
        List<Row> schoolOverAverageRows = dao.query(SCHOOL_PROJECT_OVER_AVERAGE_RATE.replace("{{table}}", "score_project"));
        sheetContext.rowAdd(schoolOverAverageRows);

        //全科及格率 、全科不及格率
        sheetContext.rowAdd(dao.query(PASS_OR_FAIL));
        return rows;
    }

    private Row getSchoolSubjectTotalRow(DAO dao, String subjectId) {
        Row totalRow = dao.queryFirst(SUBJECT_TOTAL_ROW.replace("{{subjectId}}", subjectId));

        double averageScore = totalRow.getDouble("average_score", 0);

        String totalOverAverageRateSql = TOTAL_OVER_AVERAGE_RATE
                .replace("{{table}}", SUBJECT + subjectId)
                .replace("{{averageScore}}", String.valueOf(averageScore));

        Row totalOverAverageRate = dao.queryFirst(totalOverAverageRateSql);
        String overRate = String.format("%.02f%%",
                NumberUtil.scale(100.0 * totalOverAverageRate.getDouble("over_average", 0), 2));
        totalRow.put("over_average", overRate);
        return totalRow;
    }

    private List<Row> putSchoolSubjectInfo(SheetContext sheetContext, DAO dao, String subjectId) {
        String sql = SCHOOL_PROJECT_OR_SUBJECT_INFO
                .replace("{{table}}", SUBJECT + subjectId)
                .replace("{{targetType}}", getTargetType(sheetContext))
                .replace("{{targetId}}", subjectId);
        List<Row> rows = dao.query(sql);
        sheetContext.rowAdd(rows);

        //超均率
        List<Row> schoolSubjectOverAverageRows = dao.query(
                SCHOOL_PROJECT_OVER_AVERAGE_RATE.replace("{{table}}", SUBJECT + subjectId));
        sheetContext.rowAdd(schoolSubjectOverAverageRows);
        return rows;
    }

    private void putNoteRow(SheetContext sheetContext, String projectId) {
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        JSONObject jsonObject = JSONArray.parseObject(reportConfig.getScoreLevels());

        String xlnt = jsonObject.getDouble("Excellent") * 100 + "%";
        String good = jsonObject.getDouble("Good") * 100 + "%";
        String pass = jsonObject.getDouble("Pass") * 100 + "%";
        String text = NOTE
                .replace("{{xlnt}}", xlnt)
                .replace("{{good}}", good)
                .replace("{{pass}}", pass);
        Row noteRow = new Row();
        noteRow.put("school_id", "note");
        noteRow.put("school_name", text);
        sheetContext.rowAdd(noteRow);
    }


    private void accordingAverageSorting(SheetContext sheetContext, List<Row> rows) {
        addAverageScoreRange(rows, sheetContext, "average_score", "average_range");
        sheetContext.rowSortBy("average_range");
    }


    private void putTableHeader(SheetContext sheetContext, Map<String, String> tableHeader) {
        sheetContext.headerPut("联考学校分数统计分析" + getSubjectName(sheetContext), 1, tableHeader.size());
        sheetContext.headerMove(Direction.DOWN);

        sheetContext.tableSetKey("school_id");
        int index = 0;
        for (Map.Entry<String, String> entry : tableHeader.entrySet()) {
            sheetContext.headerPut(entry.getValue(), 2, 1);
            sheetContext.columnSet(index, entry.getKey());
            sheetContext.columnWidth(index, 15);
            sheetContext.headerMove(Direction.RIGHT);
            index++;
        }

        sheetContext.columnWidth(0, 20);   // 学校名称字段约 20 个字符宽
        sheetContext.headerMove(Direction.DOWN);
    }


    private void addAverageScoreRange(List<Row> rows, SheetContext sheetContext, String scoreColumnName, String rankColumnName) {
        Ranker<String> ranker = new Ranker<>();

        rows.forEach(row -> {
            String schoolId = row.getString("school_id");
            double score = row.getDouble(scoreColumnName, 0);
            ranker.put(schoolId, score);
        });

        rows.forEach(row -> {
            String schoolId = row.getString("school_id");
            int rank = ranker.getRank(schoolId, false);
            sheetContext.tablePutValue(schoolId, rankColumnName, rank+1);

        });
    }


    protected abstract Map<String, String> getTableHeader();

    protected abstract String getTargetType(SheetContext sheetContext);

    protected abstract String getSubjectName(SheetContext sheetContext);

    protected abstract String getSubjectId(SheetContext sheetContext);

}
