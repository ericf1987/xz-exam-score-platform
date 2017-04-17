package com.xz.scorep.executor.aggregate.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import com.xz.scorep.executor.reportconfig.ScoreLevelsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    private static final String PROVINCE_COUNT = "select count(*) as cnt from score_project";

    private static final String SCHOOL_COUNT = "select student.school_id, count(*) as cnt\n" +
            "from score_project s, student where s.student_id=student.id\n" +
            "group by student.school_id";

    private static final String CLASS_COUNT = "select student.class_id, count(*) as cnt\n" +
            "from score_project s, student where s.student_id=student.id\n" +
            "group by student.class_id";


    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table all_pass_or_fail ");
        LOG.info("全科及格率、不及格率表已清空");

        //////////////////////////////////////////////////////////////

        int provinceStudentCount = projectDao.count(PROVINCE_COUNT);

        Map<String, Integer> schoolStudentCount = projectDao.query(SCHOOL_COUNT)
                .stream().collect(Collectors.toMap(
                        row -> row.getString("school_id"),
                        row -> row.getInteger("cnt", 0)
                ));

        Map<String, Integer> classStudentCount = projectDao.query(CLASS_COUNT)
                .stream().collect(Collectors.toMap(
                        row -> row.getString("class_id"),
                        row -> row.getInteger("cnt", 0)
                ));

        //////////////////////////////////////////////////////////////

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        JSONObject scoreLevels = JSON.parseObject(reportConfig.getScoreLevels());

        // subjectId -> 及格分数(double)
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        Map<String, Double> passScores = subjects
                .stream().collect(Collectors.toMap(
                        ExamSubject::getId, subject -> ScoreLevelsHelper.passScore(subject.getId(), scoreLevels, subject.getFullScore())));

        String sql = generateSql(subjects, passScores);

        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql);

        //////////////////////////////////////////////////////////////

        aggregateData(projectDao, rows, provinceStudentCount, schoolStudentCount, classStudentCount);
    }

    private String generateSql(List<ExamSubject> subjects, Map<String, Double> passScores) {
        StringBuilder sqlBuilder = new StringBuilder("select st.id as student_id,st.class_id,st.school_id,");

        // if (score_001>=80 and score_002>=80 and ..., 'true', 'false') as all_pass
        String allPass = "if (" + String.join(" and ", subjects.stream().map(
                subject -> "score_" + subject.getId() + ">=" + passScores.get(subject.getId()))
                .collect(Collectors.toList())) + ", 'true', 'false') as all_pass";

        // if (score_001<80 and score_002<80 and ..., 'true', 'false') as all_fail
        String allFail = "if (" + String.join(" and ", subjects.stream().map(
                subject -> "score_" + subject.getId() + "<" + passScores.get(subject.getId()))
                .collect(Collectors.toList())) + ", 'true', 'false') as all_fail";

        sqlBuilder.append(allPass).append(",").append(allFail).append(" from (select s.student_id,");

        String scoreColumns = String.join(",", subjects.stream().map(
                subject -> "ifnull(score_subject_" + subject.getId() + ".score,0) as score_" + subject.getId())
                .collect(Collectors.toList()));

        sqlBuilder.append(scoreColumns).append(" from score_project s ");

        String leftJoins = String.join(" ", subjects.stream().map(
                subject -> "left join score_subject_" + subject.getId() + " using(student_id)")
                .collect(Collectors.toList()));

        sqlBuilder.append(leftJoins).append(") a, student st where a.student_id=st.id");
        return sqlBuilder.toString();
    }

    private void aggregateData(DAO projectDao, List<Row> rows,
                               int provinceStudentCount,
                               Map<String, Integer> schoolStudentCount,
                               Map<String, Integer> classStudentCount) {

        AtomicInteger provinceAllPassCount = new AtomicInteger();
        AtomicInteger provinceAllFailCount = new AtomicInteger();

        CounterMap<String> schoolAllPassCount = new CounterMap<>();
        CounterMap<String> schoolAllFailCount = new CounterMap<>();
        CounterMap<String> classAllPassCount = new CounterMap<>();
        CounterMap<String> classAllFailCount = new CounterMap<>();

        rows.forEach(row -> {
            if (row.get("all_pass").equals("true")) {
                provinceAllPassCount.incrementAndGet();
                schoolAllPassCount.incre(row.getString("school_id"));
                classAllPassCount.incre(row.getString("class_id"));

            } else if (row.get("all_fail").equals("true")) {
                provinceAllFailCount.incrementAndGet();
                schoolAllFailCount.incre(row.getString("school_id"));
                classAllFailCount.incre(row.getString("class_id"));
            }
        });

        List<Map<String, Object>> resultRows = new ArrayList<>();

        resultRows.add(createMap(Range.PROVINCE, Range.PROVINCE_RANGE.getId(),
                provinceStudentCount, provinceAllPassCount.get(), provinceAllFailCount.get()));

        schoolStudentCount.forEach((schoolId, totalCount) -> {
            int allPassCount = getCount(schoolAllPassCount, schoolId);
            int allFailCount = getCount(schoolAllFailCount, schoolId);
            resultRows.add(createMap(Range.SCHOOL, schoolId, totalCount, allPassCount, allFailCount));
        });

        classStudentCount.forEach((classId, totalCount) -> {
            int allPassCount = getCount(classAllPassCount, classId);
            int allFailCount = getCount(classAllFailCount, classId);
            resultRows.add(createMap(Range.CLASS, classId, totalCount, allPassCount, allFailCount));
        });

        projectDao.insert(resultRows, "all_pass_or_fail");
    }

    private int getCount(Map<String, Integer> map, String key) {
        return map.getOrDefault(key, 0);
    }

    private Map<String, Object> createMap(String rangeType, String rangeId, int totalCount, int allPassCount, int allFailCount) {
        Map<String, Object> map = new HashMap<>();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("all_pass_count", allPassCount);
        map.put("all_pass_rate", (double) allPassCount / totalCount * 100);
        map.put("all_fail_count", allFailCount);
        map.put("all_fail_rate", (double) allFailCount / totalCount * 100);
        return map;
    }
}
