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
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Author: luckylo
 * Date : 2017-02-27
 */
@Component
public class TotalAverageSheet0 extends SheetGenerator {

    private static final String TOTAL_SCHOOL_ID = "t";

    private static final String SUBJECT = "score_subject_";

    private static String SCHOOL_PROJECT_INFO = "SELECT\n" +
            " a.school_id,\n" +
            " a.school_name,\n" +
            " a.student_count,\n" +
            " a.max_score,\n" +
            " a.min_score,\n" +
            " a.average_score,\n" +
            " CONCAT(IFNULL(xlnt.xlnt, '0.00'),'%') AS excellent,\n" +
            " CONCAT(IFNULL(good.good, '0.00'),'%') AS good,\n" +
            " CONCAT(IFNULL(pass.pass, '0.00'),'%') AS pass,\n" +
            " CONCAT(IFNULL(fail.fail, '0.00'),'%') AS fail\n" +
            " FROM\n" +
            " (\n" +
            " SELECT\n" +
            " school.id AS school_id,\n" +
            " school. NAME AS school_name,\n" +
            " COUNT(student.id) student_count,\n" +
            " MAX(score_project.score) AS max_score,\n" +
            " MIN(score_project.score) AS min_score,\n" +
            " FORMAT(AVG(score_project.score),2) AS average_score\n" +
            " FROM\n" +
            " school,\n" +
            " student,\n" +
            " score_project\n" +
            " WHERE\n" +
            " school.id = student.school_id\n" +
            " AND student.id = score_project.student_id\n" +
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
            " AND scorelevelmap.target_type = 'project'\n" +
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
            " AND scorelevelmap.target_type = 'project'\n" +
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
            " AND scorelevelmap.target_type = 'project'\n" +
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
            " AND scorelevelmap.target_type = 'project'\n" +
            " AND scorelevelmap.range_id = school.id\n" +
            " AND scorelevelmap.score_level = 'FAIL'\n" +
            " ) fail ON fail.id = a.school_id";


    private static String ALL_SUBJECT_PASS_OR_FAIL = "select school.id AS school_id , COUNT(school.id) as count from " +
            "school,student {{table_name}} where " +
            "student.school_id = school.id {{sub}} {{passOrFail}} GROUP BY school.id";


    private static String TOTAL_SCORE_LEVEL = "select \n" +
            "  @total := (select count(1) from score_project) as total, \n" +
            "  @average  := (select count(1) from score_project where score >= {{average_score}})as average_count, \n" +
            "  FORMAT(@average / @total,4) as average_rate, \n" +
            "  @fail  :=(select count(1) from score_project where score < {{fail_score}}) as fail_count, \n" +
            "  FORMAT(@fail / @total,4) as fail_rate, \n" +
            "  @pass  := (select count(1) from score_project where score >= {{fail_score}} and score < {{good_score}}) as pass_count, \n" +
            "  FORMAT(@pass / @total,4) as pass_rate, \n" +
            "  @good  := (select count(1) from score_project where score >= {{good_score}} and score < {{excellent_score}}) as good_count, \n" +
            "  FORMAT(@good / @total,4) as good_rate, \n" +
            "  @xlnt  := (select count(1) from score_project where score >= {{excellent_score}}) as xlnt_count, \n" +
            "  FORMAT(@xlnt / @total,4) as xlnt_rate \n" +
            ";\n";


    private static String SCHOOL_OVER_AVERAGE_RATE = "select student.school_id ,CONCAT(FORMAT(COUNT(student.id)/a.count,2),'%') as over_average from student\n" +
            " LEFT JOIN score_project on student.id = score_project.student_id\n" +
            " LEFT JOIN (SELECT\n" +
            " school.id  as school_id,\n" +
            " COUNT(student.id) as count,\n" +
            " FORMAT(AVG(score_project.score),2) AS average_score\n" +
            " FROM\n" +
            " school,\n" +
            " student,\n" +
            " score_project\n" +
            " WHERE\n" +
            " school.id = student.school_id\n" +
            " AND student.id = score_project.student_id\n" +
            " GROUP BY\n" +
            " school.id) a on a.school_id = student.school_id\n" +
            " where score_project.score >= a.average_score GROUP BY student.school_id";

    private static final String[] TABLE_HEADER = {
            "学校名称", "实考人数", "最高分",
            "最低分", "平均分", "平均分排名",
            "优率", "良率", "及格率", "不及格率",
            "超均率", "全科及格率", "全科不及格率"
    };

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        sheetContext.headerPut("联考学校分数统计分析（总分）", 1, 13);
        sheetContext.headerMove(Direction.DOWN);

        for (String string : TABLE_HEADER) {
            sheetContext.headerPut(string, 2, 1);
            sheetContext.headerMove(Direction.RIGHT);
        }

        sheetContext.columnWidth(0, 24);   // 学校名称字段约 24 个字符宽

        sheetContext.tableSetKey("school_id");
        sheetContext.columnSet(0, "school_name");   //学校名
        sheetContext.columnSet(1, "student_count");   //实考人数
        sheetContext.columnSet(2, "max_score");  //最高分
        sheetContext.columnSet(3, "min_score");   //最低分
        sheetContext.columnSet(4, "average_score");   //平均分

        sheetContext.columnSet(5, "average_range");   //平均分排名

        sheetContext.columnSet(6, "excellent");   //优率
        sheetContext.columnSet(7, "good");   //良率
        sheetContext.columnSet(8, "pass");   //及格率
        sheetContext.columnSet(9, "fail");   //不及格率
        sheetContext.columnSet(10, "over_average");   //超均率
        sheetContext.columnSet(11, "all_pass");   //全科及格率
        sheetContext.columnSet(12, "all_fail");   //全科不及格率

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);
        DAO managerDao = daoFactory.getManagerDao();
        Row projectScore = managerDao.queryFirst("select full_score from project where id = ?", projectId);
        Double fullScore = projectScore.getDouble("full_score", 0);

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        JSONObject jsonObject = JSONArray.parseObject(reportConfig.getScoreLevels());

        double excellentScore = jsonObject.getDouble("Excellent") * fullScore;
        double goodScore = jsonObject.getDouble("Good") * fullScore;
        double passScore = jsonObject.getDouble("Pass") * fullScore;

        //查学校参考人数、最高分、最低分、平均分、学校四率
        List<Row> rows = dao.query(SCHOOL_PROJECT_INFO);
        sheetContext.rowAdd(rows);

        //超均率
        List<Row> overAverageRows = dao.query(SCHOOL_OVER_AVERAGE_RATE);
        sheetContext.rowAdd(overAverageRows);

        List<Row> subjects = dao.query("select id,full_score from subject");

        StringBuffer table_name = new StringBuffer("");
        StringBuffer sub = new StringBuffer("");
        StringBuffer passStr = new StringBuffer("");
        StringBuffer failStr = new StringBuffer("");
        for (Row row : subjects) {
            String id = row.getString("id");
            double subjectScore = row.getDouble("full_score", 0) * jsonObject.getDouble("Pass");

            table_name.append("," + SUBJECT + id);
            sub.append("AND " + SUBJECT + id + ".student_id = " + "student.id ");
            passStr.append(" AND " + SUBJECT + id + ".score >= " + subjectScore);
            failStr.append(" AND " + SUBJECT + id + ".score < " + subjectScore);
        }
        String passSql = ALL_SUBJECT_PASS_OR_FAIL
                .replace("{{table_name}}", table_name.toString())
                .replace("{{sub}}", sub.toString())
                .replace("{{passOrFail}}", passStr.toString());
        System.out.println(passSql);
        String failSql = ALL_SUBJECT_PASS_OR_FAIL
                .replace("{{table_name}}", table_name.toString())
                .replace("{{sub}}", sub.toString())
                .replace("{{passOrFail}}", failStr.toString());

        List<Row> passRows = dao.query(passSql);
        List<Row> failRows = dao.query(failSql);
        double totalPass = 0;
        double totalFail = 0;
        for (Row row : rows) {
            String schoolId = row.getString("school_id");
            double studentCount = row.getDouble("student_count", 1);
            boolean passFlag = false;
            boolean failFlag = false;

            for (Row passRow : passRows) {
                if (passRow.getString("school_id").equals(schoolId)) {
                    passFlag = true;
                    double count = passRow.getDouble("count", 0);
                    totalPass += count;
                    String passRate = String.format("%.02f%%",
                            NumberUtil.scale(100.0 * count / studentCount, 2));
                    sheetContext.tablePutValue(schoolId, "all_pass", passRate);
                }
            }
            for (Row failRow : failRows) {
                if (failRow.getString("school_id").equals(schoolId)) {
                    failFlag = true;
                    double count = failRow.getDouble("count", 0);
                    totalFail += count;
                    String failRate = String.format("%.02f%%",
                            NumberUtil.scale(100.0 * count / studentCount, 2));
                    sheetContext.tablePutValue(schoolId, "all_fail", failRate);
                }
            }
            if (!passFlag) {
                sheetContext.tablePutValue(schoolId, "all_pass", "0.00%");
            }
            if (!failFlag) {
                sheetContext.tablePutValue(schoolId, "all_fail", "0.00%");
            }
        }

        //先按平均分排名,最后增加总计行
        addAverageScoreRange(rows, sheetContext, "average_score", "average_range");
        sheetContext.rowSortBy("average_range");

        Row totalRow = new Row();
        double averageScore = rows.stream()
                .mapToDouble(row -> row.getDouble("average_score", 0))
                .sum() / rows.size();
        String sql = TOTAL_SCORE_LEVEL
                .replace("{{average_score}}", String.valueOf(averageScore))
                .replace("{{fail_score}}", String.valueOf(passScore))
                .replace("{{good_score}}", String.valueOf(goodScore))
                .replace("{{excellent_score}}", String.valueOf(excellentScore));

        Row total = dao.queryFirst(sql);
        addTotalRowContent(rows, total, totalFail, totalPass, totalRow);   //填充总计行

        sheetContext.rowAdd(totalRow);
        sheetContext.rowStyle(TOTAL_SCHOOL_ID, ExcelCellStyles.Green.name());

        sheetContext.saveData();                      // 保存到 ExcelWriter
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
            sheetContext.tablePutValue(schoolId, rankColumnName, rank);

        });
    }


    private void addTotalRowContent(List<Row> rows, Row total, double totalFail, double totalPass, Row totalRow) {
        totalRow.put("school_id", TOTAL_SCHOOL_ID);
        totalRow.put("school_name", "总体");
        int studentCount = rows.stream()
                .mapToInt(row -> row.getInteger("student_count", 0))
                .sum();
        double maxScore = rows.stream()
                .mapToDouble(row -> row.getDouble("max_score", 0))
                .summaryStatistics()
                .getMax();
        double minScore = rows.stream()
                .mapToDouble(row -> row.getDouble("min_score", 0))
                .summaryStatistics()
                .getMin();
        double averageScore = rows.stream()
                .mapToDouble(row -> row.getDouble("average_score", 0))
                .sum() / rows.size();
        //学生总数   最高分   最低分  平均分
        totalRow.put("student_count", studentCount);
        totalRow.put("max_score", maxScore);
        totalRow.put("min_score", minScore);
        totalRow.put("average_score", NumberUtil.scale(averageScore, 2));
        totalRow.put("average_range", "--");

        totalRow.put("excellent", String.format("%.02f%%",
                NumberUtil.scale(total.getDouble("xlnt_rate", 0) * 100, 2)));
        totalRow.put("good", String.format("%.02f%%",
                NumberUtil.scale(total.getDouble("good_rate", 0) * 100, 2)));
        totalRow.put("pass", String.format("%.02f%%",
                NumberUtil.scale(total.getDouble("pass_rate", 0) * 100, 2)));
        totalRow.put("fail", String.format("%.02f%%",
                NumberUtil.scale(total.getDouble("fail_rate", 0) * 100, 2)));
        totalRow.put("over_average", String.format("%.02f%%",
                NumberUtil.scale(total.getDouble("average_rate", 0) * 100, 2)));

        String passRate = String.format("%.02f%%",
                NumberUtil.scale(100.0 * totalPass / studentCount, 2));
        String failRate = String.format("%.02f%%",
                NumberUtil.scale(100.0 * totalFail / studentCount, 2));
        totalRow.put("all_pass", passRate);
        totalRow.put("all_fail", failRate);

    }
}
