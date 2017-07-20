package com.xz.scorep.executor.aggregate.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.lang.CounterMap;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.aggregate.AggregateType;
import com.xz.scorep.executor.aggregate.AggregateTypes;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
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
 * 统计全科优秀率和良好率
 *
 * @author caijianghua
 */
@AggregateTypes(AggregateType.Basic)
@AggregateOrder(16)
@Component
public class AllExcellentGoodFailAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(AllExcellentGoodFailAggregator.class);

    private static final String PROVINCE_COUNT = "select count(*) as cnt from score_project";

    private static final String SCHOOL_COUNT = "select student.school_id, count(*) as cnt\n" +
            "from score_project s, student where s.student_id=student.id\n" +
            "group by student.school_id";

    private static final String CLASS_COUNT = "select student.class_id, count(*) as cnt\n" +
            "from score_project s, student where s.student_id=student.id\n" +
            "group by student.class_id";


    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        projectDao.execute("truncate table all_excellent_or_good");

        LOG.info("全科优秀率、良好率已清空");

        int provinceStudentCount = projectDao.count(PROVINCE_COUNT);

        Map<String, Integer> schoolStudentCount = projectDao.query(SCHOOL_COUNT)
                .stream().collect(Collectors.toMap(
                        row -> row.getString("school_id"),
                        row -> row.getInteger("cnt", 0)));

        Map<String, Integer> classStudentCount = projectDao.query(CLASS_COUNT)
                .stream().collect(Collectors.toMap(
                        row -> row.getString("class_id"),
                        row -> row.getInteger("cnt", 0)
                ));

        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        String scoreLevelConfig = reportConfig.getScoreLevelConfig();
        JSONObject scoreLevels = JSON.parseObject(reportConfig.getScoreLevels());

        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        Map<String, Double> excellentScore = subjects.stream().collect(Collectors.toMap(
                ExamSubject::getId, subject -> ScoreLevelsHelper.excellentScore(
                        subject.getId(), scoreLevels, scoreLevelConfig, subject.getFullScore())
        ));

        Map<String, Double> goodScore = subjects.stream().collect(Collectors.toMap(
                ExamSubject::getId, subject -> ScoreLevelsHelper.goodScore(
                        subject.getId(), scoreLevels, scoreLevelConfig, subject.getFullScore()
                )
        ));
        String sql = generateSql(subjects, goodScore, excellentScore);
        System.out.println(excellentScore);
        System.out.println(sql);

        List<Row> rows = projectDao.query(sql);


        aggregateData(projectDao, rows, provinceStudentCount, schoolStudentCount, classStudentCount);
        LOG.info("项目ID {}  全科优秀全科良好率统计完成....", projectId);
    }

    private String generateSql(List<ExamSubject> subjects, Map<String, Double> goodScores,
                               Map<String, Double> excellentScore) {
        StringBuilder sqlBuilder = new StringBuilder("select st.id as student_id,st.class_id,st.school_id,");

        // if (score_001>=90 and score_002>=90 and ..., 'true', 'false') as all_pass
        String allPass = "if (" + String.join(" and ", subjects.stream().map(
                subject -> "score_" + subject.getId() + ">=" + excellentScore.get(subject.getId()))
                .collect(Collectors.toList())) + ", 'true', 'false') as all_excellent";

        // if (score_001>=80 and score_002>=80 and ..., 'true', 'false') as all_pass
        String allFail = "if (" + String.join(" and ", subjects.stream().map(
                subject -> "score_" + subject.getId() + ">=" + goodScores.get(subject.getId()))
                .collect(Collectors.toList())) + ", 'true', 'false') as all_good";

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
        if (provinceStudentCount == 0) {
            throw new IllegalArgumentException("Student count is 0");
        }

        AtomicInteger provinceAllExcellentCount = new AtomicInteger();
        AtomicInteger provinceAllGoodCount = new AtomicInteger();

        CounterMap<String> schoolAllExcellentCount = new CounterMap<>();
        CounterMap<String> schoolAllGoodCount = new CounterMap<>();

        CounterMap<String> classAllExcellentCount = new CounterMap<>();
        CounterMap<String> classAllGoodCount = new CounterMap<>();

        rows.forEach(row -> {
            if (row.get("all_excellent").equals("true")) {
                provinceAllExcellentCount.incrementAndGet();
                schoolAllExcellentCount.incre(row.getString("school_id"));
                classAllExcellentCount.incre(row.getString("class_id"));
            } else if (row.get("all_good").equals("true")) {
                provinceAllGoodCount.incrementAndGet();
                schoolAllGoodCount.incre(row.getString("school_id"));
                classAllGoodCount.incre(row.getString("class_id"));
            }
        });
        List<Map<String, Object>> resultRows = new ArrayList<>();
        resultRows.add(createMap(Range.PROVINCE, Range.PROVINCE_RANGE.getId(),
                provinceStudentCount, provinceAllExcellentCount.get(), provinceAllGoodCount.get()));

        schoolStudentCount.forEach((schoolId, totalCount) -> {
            int allExcellentCount = getCount(schoolAllExcellentCount, schoolId);
            int allGoodCount = getCount(schoolAllGoodCount, schoolId);

            if (totalCount == 0) {
                LOG.warn("Student count is 0 [schoolId=" + schoolId + "]");
            } else {
                resultRows.add(createMap(Range.SCHOOL, schoolId, totalCount, allExcellentCount, allGoodCount));
            }
        });

        classStudentCount.forEach((classId, totalCount) -> {
            int allExcellentCount = getCount(classAllExcellentCount, classId);
            int allGoodCount = getCount(classAllGoodCount, classId);

            if (totalCount == 0) {
                LOG.warn("Student count is 0 [classId = " + classId + "]");
            } else {
                resultRows.add(createMap(Range.CLASS, classId, totalCount, allExcellentCount, allGoodCount));
            }
        });

        projectDao.insert(resultRows, "all_excellent_or_good");
    }

    private int getCount(Map<String, Integer> map, String key) {
        return map.getOrDefault(key, 0);
    }

    private Map<String, Object> createMap(String rangeType, String rangeId, int totalCount, int allExcellentCount, int allGoodCount) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("all_excellent_count", allExcellentCount);
        map.put("all_excellent_rate", (double) allExcellentCount / totalCount * 100);
        map.put("all_good_count", allGoodCount);
        map.put("all_good_rate", (double) allGoodCount / totalCount * 100);
        return map;
    }
}
