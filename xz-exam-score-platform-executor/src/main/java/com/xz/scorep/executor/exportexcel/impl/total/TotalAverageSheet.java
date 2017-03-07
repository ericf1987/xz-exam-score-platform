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
import com.xz.scorep.executor.project.ProjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.utils.Direction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Author: luckylo
 * Date : 2017-03-02
 */

public abstract class TotalAverageSheet extends SheetGenerator {
    private static final String TOTAL_SCHOOL_ID = "t";

    private static final String SUBJECT = "score_subject_";

    private static String SCHOOL_PROJECT_OR_SUBJECT_INFO = "SELECT\n" +
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


    private static String ALL_SUBJECT_PASS_OR_FAIL = "select school.id AS school_id , COUNT(school.id) as count from " +
            "school,student {{table_name}} where " +
            "student.school_id = school.id {{sub}} {{passOrFail}} GROUP BY school.id";


    private static String TOTAL_SCORE_LEVEL = "select \n" +
            "  @total := (select count(1) from {{table}}) as total, \n" +
            "  @average  := (select count(1) from {{table}} where score >= {{average_score}})as average_count, \n" +
            "  FORMAT(@average / @total,4) as average_rate, \n" +
            "  @fail  :=(select count(1) from {{table}} where score < {{fail_score}}) as fail_count, \n" +
            "  FORMAT(@fail / @total,4) as fail_rate, \n" +
            "  @pass  := (select count(1) from {{table}} where score >= {{fail_score}} and score < {{good_score}}) as pass_count, \n" +
            "  FORMAT(@pass / @total,4) as pass_rate, \n" +
            "  @good  := (select count(1) from {{table}} where score >= {{good_score}} and score < {{excellent_score}}) as good_count, \n" +
            "  FORMAT(@good / @total,4) as good_rate, \n" +
            "  @xlnt  := (select count(1) from {{table}} where score >= {{excellent_score}}) as xlnt_count, \n" +
            "  FORMAT(@xlnt / @total,4) as xlnt_rate \n" +
            ";\n";


    private static String SCHOOL_PROJECT_OVER_AVERAGE_RATE = "select student.school_id ,CONCAT(FORMAT(COUNT(student.id)/a.count,2),'%') as over_average from student\n" +
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


    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    ProjectService projectService;

    @Override
    protected void generateSheet(SheetContext sheetContext) throws Exception {
        generateEachSheet(sheetContext);
    }

    protected void generateEachSheet(SheetContext sheetContext) {
        Map<String, String> tableHeader = getTableHeader();
        putTableHeader(sheetContext, tableHeader);

        String projectId = sheetContext.getProjectId();
        DAO dao = daoFactory.getProjectDao(projectId);

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        JSONObject jsonObject = JSONArray.parseObject(reportConfig.getScoreLevels());

        Row totalRow = new Row();       //总计行
        totalRow.put("school_id", TOTAL_SCHOOL_ID);
        totalRow.put("school_name", "总体");

        if (tableHeader.get("all_pass") == null) {//单科
            String subjectId = getSubjectId(sheetContext);
            //查每个学校每科的人数、最高分、最低分、平均分、四率
            String sql = SCHOOL_PROJECT_OR_SUBJECT_INFO
                    .replace("{{table}}", SUBJECT + subjectId)
                    .replace("{{targetType}}", getTargetType(sheetContext))
                    .replace("{{targetId}}", subjectId);
            List<Row> rows = dao.query(sql);
            sheetContext.rowAdd(rows);

            //超均率
            List<Row> overAverageRows = dao.query(
                    SCHOOL_PROJECT_OVER_AVERAGE_RATE.replace("{{table}}", SUBJECT + subjectId));
            sheetContext.rowAdd(overAverageRows);

            //先按平均分排名,最后增加总计行
            accordingAverageSorting(sheetContext, rows);

            double fullScore = getSubjectFullScore(dao, subjectId);
            String projectTotalSql = getTotalScoreLevel(rows, fullScore, jsonObject, SUBJECT + subjectId);

            Row row = dao.queryFirst(projectTotalSql);
            addTotalRowContent(rows, row, 0, 0, totalRow,false);   //填充总计行
        } else {//全科
            //查学校参考人数、最高分、最低分、平均分、四率
            String sql = SCHOOL_PROJECT_OR_SUBJECT_INFO
                    .replace("{{table}}", "score_project")
                    .replace("{{targetType}}", getTargetType(sheetContext))
                    .replace("{{targetId}}", projectId);

            List<Row> rows = dao.query(sql);
            sheetContext.rowAdd(rows);

            //超均率
            List<Row> overAverageRows = dao.query(SCHOOL_PROJECT_OVER_AVERAGE_RATE.replace("{{table}}", "score_project"));
            sheetContext.rowAdd(overAverageRows);

            //全科及格率 、全科不及格率
            List<Row> passRows = dao.query(getExecuteSql(dao, jsonObject, true));
            List<Row> failRows = dao.query(getExecuteSql(dao, jsonObject, false));

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
            accordingAverageSorting(sheetContext, rows);


            double fullScore = projectService.findProject(projectId).getFullScore();
            String projectTotalSql = getTotalScoreLevel(rows, fullScore, jsonObject, "score_project");

            Row row = dao.queryFirst(projectTotalSql);
            addTotalRowContent(rows, row, totalFail, totalPass, totalRow,true);   //填充总计行

        }


        sheetContext.rowAdd(totalRow);
        sheetContext.rowStyle(TOTAL_SCHOOL_ID, ExcelCellStyles.Green.name());
        sheetContext.saveData();// 保存到 ExcelWriter
    }

    protected double getSubjectFullScore(DAO dao, String subjectId) {
        Row row = dao.queryFirst("select full_score from subject where id = ?", subjectId);
        return row.getDouble("full_score", 0);
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

        sheetContext.freeze(0,2);
        sheetContext.columnWidth(0, 20);   // 学校名称字段约 20 个字符宽
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


    private void addTotalRowContent(List<Row> rows,
                 Row total, double totalFail, double totalPass,
                 Row totalRow,boolean allSubjects) {

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

        if (allSubjects){
            String passRate = String.format("%.02f%%",
                    NumberUtil.scale(100.0 * totalPass / studentCount, 2));
            String failRate = String.format("%.02f%%",
                    NumberUtil.scale(100.0 * totalFail / studentCount, 2));
            totalRow.put("all_pass", passRate);
            totalRow.put("all_fail", failRate);
        }

    }


    protected abstract Map<String, String> getTableHeader();

    protected abstract String getTargetType(SheetContext sheetContext);

    protected abstract String getSubjectName(SheetContext sheetContext);

    protected abstract String getSubjectId(SheetContext sheetContext);

    public String getExecuteSql(DAO dao, JSONObject jsonObject, boolean pass) {
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

        if (pass) {
            return ALL_SUBJECT_PASS_OR_FAIL
                    .replace("{{table_name}}", table_name.toString())
                    .replace("{{sub}}", sub.toString())
                    .replace("{{passOrFail}}", passStr.toString());
        } else {
            return ALL_SUBJECT_PASS_OR_FAIL
                    .replace("{{table_name}}", table_name.toString())
                    .replace("{{sub}}", sub.toString())
                    .replace("{{passOrFail}}", failStr.toString());
        }
    }

    private String getTotalScoreLevel(List<Row> rows, double fullScore, JSONObject jsonObject, String tableName) {

        double excellentScore = jsonObject.getDouble("Excellent") * fullScore;
        double goodScore = jsonObject.getDouble("Good") * fullScore;
        double passScore = jsonObject.getDouble("Pass") * fullScore;

        double averageScore = rows.stream()
                .mapToDouble(row -> row.getDouble("average_score", 0))
                .sum() / rows.size();

        return TOTAL_SCORE_LEVEL
                .replace("{{table}}", tableName)
                .replace("{{average_score}}", String.valueOf(averageScore))
                .replace("{{fail_score}}", String.valueOf(passScore))
                .replace("{{good_score}}", String.valueOf(goodScore))
                .replace("{{excellent_score}}", String.valueOf(excellentScore));
    }
}
