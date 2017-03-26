package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计全科及格率和全科不及格率
 *
 * @author luckylo
 */
@AggragateOrder(7)
@AggregateTypes(AggregateType.Basic)
@Component
public class AllPassOrFailAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(AllPassOrFailAggregator.class);

    public static String SQL = "select student.id as student_id,student.class_id,student.school_id " +
            " {{cols} from student {{tables}} where {{sub}}";

    public static final String PROVINCE_COUNT = "select count(student.id) as count from student";

    public static final String SCHOOL_COUNT = "select school.name ,school.id ,COUNT(student.id) as count from student,score_project,school \n" +
            "where school.id = student.school_id \n" +
            "and score_project.student_id = student.id \n"+
            "GROUP BY school.id";

    public static final String CLASS_COUNT = "select class.id ,COUNT(student.id) as count from student,score_project,class \n" +
            " where class.id = student.class_id \n" +
            "and score_project.student_id = student.id \n"+
            "GROUP BY class.id";


    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ReportConfigService reportConfigService;


    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table all_pass_or_fail ");
        LOG.info("全科及格率、不及格率表已清空");

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        JSONObject scoreLevels = JSON.parseObject(reportConfig.getScoreLevels());

        List<Row> subjects = projectDao.query("select * from subject");

        StringBuffer cols = new StringBuffer();
        StringBuffer tables = new StringBuffer();
        StringBuffer sub = new StringBuffer();

        double rate = scoreLevels.getDouble("Pass");
        Map<String, Double> score = new HashMap<>();

        for (Row row : subjects) {
            String id = row.getString("id");
            score.put("score_" + id, row.getDouble("full_score", 0) * rate);

            cols.append(",score_subject_" + id + ".score as score_" + id);
            tables.append(",score_subject_" + id);
            sub.append(" student.id = score_subject_" + id + ".student_id and");
        }
        String str = sub.substring(0, sub.lastIndexOf("and")).toString();

        String sql = SQL
                .replace("{{cols}", cols.toString())
                .replace("{{tables}}", tables.toString())
                .replace("{{sub}}", str);

        List<Row> result = projectDao.query(sql);

        List<Row> insertRows = new ArrayList<>();

        LOG.info("开始统计整体全科及格率、全科不及格率...");
        aggregatorProvincePassFailRate(projectDao, score, result, insertRows, projectId);
        LOG.info("统计整体全科及格率、全科不及格率完成...");
        insertRows.clear();

        LOG.info("开始统计学校全科及格率、全科不及格率...");
        aggregatorSchoolPassFailRate(projectDao, score, result, insertRows);
        LOG.info("统计学校全科及格率、全科不及格率完成...");
        insertRows.clear();

        LOG.info("开始统计班级全科及格率、全科不及格率...");
        aggregatorClassPassFailRate(projectDao, score, result, insertRows);
        LOG.info("统计班级全科及格率、全科不及格率完成...");
    }

    private void aggregatorProvincePassFailRate(DAO projectDao, Map<String, Double> score, List<Row> result, List<Row> insertRows, String projectId) {
        Row provinceCount = projectDao.queryFirst(PROVINCE_COUNT);
        int count = provinceCount.getInteger("count", 0);
        CounterMap<String> passCounterMap = new CounterMap<>();
        CounterMap<String> failCounterMap = new CounterMap<>();
        result.stream()
                .forEach(s -> checkStudentIsPassOrFail(score, projectId, passCounterMap, failCounterMap, s));

        Row provinceRow = new Row();
        provinceRow.put("range_type", Range.PROVINCE);
        provinceRow.put("range_id", projectId);

        provinceRow.put("all_pass_count", passCounterMap.getCount(projectId));
        provinceRow.put("all_pass_rate", getPercent(passCounterMap.getCount(projectId), count));
        provinceRow.put("all_fail_count", failCounterMap.getCount(projectId));
        provinceRow.put("all_fail_rate", getPercent(failCounterMap.getCount(projectId), count));
        insertRows.add(provinceRow);
        projectDao.insert(insertRows, "all_pass_or_fail");
    }

    private void aggregatorClassPassFailRate(DAO projectDao, Map<String, Double> score, List<Row> result, List<Row> insertRows) {
        List<Row> classCount = projectDao.query(CLASS_COUNT);
        for (Row row : classCount) {
            String classId = row.getString("id");
            int count = row.getInteger("count", 0);
            CounterMap<String> passCounterMap = new CounterMap<>();
            CounterMap<String> failCounterMap = new CounterMap<>();

            result.stream()
                    .filter(s -> s.getString("class_id").equals(classId))
                    .forEach(s -> checkStudentIsPassOrFail(score, classId, passCounterMap, failCounterMap, s));
            Row classRow = new Row();
            classRow.put("range_type", Range.CLASS);
            classRow.put("range_id", classId);

            classRow.put("all_pass_count", passCounterMap.getCount(classId));
            classRow.put("all_pass_rate", getPercent(passCounterMap.getCount(classId), count));
            classRow.put("all_fail_count", failCounterMap.getCount(classId));
            classRow.put("all_fail_rate", getPercent(failCounterMap.getCount(classId), count));
            insertRows.add(classRow);
        }

        projectDao.insert(insertRows, "all_pass_or_fail");
    }

    private void aggregatorSchoolPassFailRate(DAO projectDao, Map<String, Double> score, List<Row> result, List<Row> insertRows) {
        List<Row> schoolCount = projectDao.query(SCHOOL_COUNT);
        for (Row row : schoolCount) {
            String schoolId = row.getString("id");
            int count = row.getInteger("count", 0);
            CounterMap<String> passCounterMap = new CounterMap<>();
            CounterMap<String> failCounterMap = new CounterMap<>();

            result.stream()
                    .filter(s -> s.getString("school_id").equals(schoolId))
                    .forEach(s -> checkStudentIsPassOrFail(score, schoolId, passCounterMap, failCounterMap, s));
            Row schoolRow = new Row();
            schoolRow.put("range_type", Range.SCHOOL);
            schoolRow.put("range_id", schoolId);
            schoolRow.put("all_pass_count", passCounterMap.getCount(schoolId));
            schoolRow.put("all_pass_rate", getPercent(passCounterMap.getCount(schoolId), count));
            schoolRow.put("all_fail_count", failCounterMap.getCount(schoolId));
            schoolRow.put("all_fail_rate", getPercent(failCounterMap.getCount(schoolId), count));
            insertRows.add(schoolRow);
        }

        projectDao.insert(insertRows, "all_pass_or_fail");
    }

    private void checkStudentIsPassOrFail(Map<String, Double> score, String id, CounterMap<String> passCounterMap,
                                          CounterMap<String> failCounterMap, Row row) {
        boolean all_pass = true;
        boolean all_fail = false;
        for (Map.Entry<String, Double> entry : score.entrySet()) {
            if (row.getDouble(entry.getKey(), 0) < entry.getValue()) {
                all_pass = false;
            }
            if (row.getDouble(entry.getKey(), 0) >= entry.getValue()) {
                all_fail = true;
            }
        }
        if (all_pass) {
            passCounterMap.incre(id);
        }
        if (!all_fail) {
            failCounterMap.incre(id);
        }
    }

    private double getPercent(int count, int totalCount) {
        return totalCount == 0 ? 0 : ((count * 1.0) / totalCount * 100);
    }

}
