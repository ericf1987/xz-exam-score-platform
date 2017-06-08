package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计算T值  T值  = [ (校平均分 - 总体平均分) / 总体保准差 * 10 ] + 50
 *
 * @author luckylo
 * @createTime 2017-06-05.
 */
@Component
@AggregateTypes({AggregateType.Complete})
@AggregateOrder(75)
public class TValueAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(TValueAggregator.class);

    private static final String QUERY_PROJECT_STD = "" +
            "select * from std_deviation where target_type = 'Subject' " +
            " and target_id = '{{subjectId}}' and range_type = 'Province'";

    private static final String QUERY_PROJECT_AVERAGE_SCORE = "" +
            "select * from average_score where target_type = '{{targetType}}' " +
            "and target_id = '{{targetId}}' and range_type = '{{rangeType}}'";

    private static final String QUERY_SCHOOL_LIST = "select * from average_score where range_type = 'School' and " +
            "target_type = 'Subject' and target_id = '{{subjectId}}' ";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SchoolService schoolService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);

        //总体的T值由公式得出,恒定为50
        projectDao.execute("truncate table t_value");
        LOG.info("项目 ID {} 在统计T值  ....", projectId);
        aggregateTValue(projectDao, projectId, subjects);
    }

    private void aggregateTValue(DAO projectDao, String projectId, List<ExamSubject> subjects) {

        subjects.forEach(subject -> aggregateSubjectTValue(projectDao, projectId, subject));

    }

    private void aggregateSubjectTValue(DAO projectDao, String projectId, ExamSubject subject) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        String subjectId = subject.getId();

        double stdDeviation = projectDao
                .queryFirst(QUERY_PROJECT_STD.replace("{{subjectId}}", subjectId))
                .getDouble("std_deviation", 0);

        String averageSql = QUERY_PROJECT_AVERAGE_SCORE.replace("{{rangeType}}", "Province")
                .replace("{{targetType}}", "Subject")
                .replace("{{targetId}}", subjectId);
        double averageScore = projectDao.queryFirst(averageSql).getDouble("average_score", 0);

        List<Row> schoolList = projectDao.query(QUERY_SCHOOL_LIST.replace("{{subjectId}}", subjectId));
        schoolService.listSchool(projectId).forEach(school -> {
            String schoolId = school.getId();
            Row schoolRow = schoolList.stream().filter(row -> schoolId.equals(row.getString("range_id")))
                    .findFirst().get();
            if (null == schoolRow) {
                return;
            }

            double schoolAverage = schoolRow.getDouble("average_score", 0);
            double value = calculateTValue(stdDeviation, averageScore, schoolAverage);

            Map<String, Object> map = createMap("School", schoolId, subjectId, value);
            insertMap.add(map);
        });

        Map<String, Object> projectMap = createMap("Project", projectId, subjectId, 50);
        insertMap.add(projectMap);

        projectDao.insert(insertMap, "t_value");
    }

    private Map<String, Object> createMap(String rangeType, String rangeId, String subjectId, double value) {
        Map<String, Object> map = new HashMap<>();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("subject_id", subjectId);
        map.put("value", value);
        return map;
    }

    private double calculateTValue(double stdDeviation, double averageScore, double schoolAverage) {
        return ((schoolAverage - averageScore) / stdDeviation) * 10 + 50;
    }
}
