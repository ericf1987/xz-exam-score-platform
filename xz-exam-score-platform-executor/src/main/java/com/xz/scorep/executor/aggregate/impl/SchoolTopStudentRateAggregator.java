package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SchoolService;
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
import java.util.stream.Collectors;

/**
 * 学校尖子生比例统计
 *
 * @author luckylo
 * @createTime 2017-06-12.
 */
@Component
@AggregateTypes({AggregateType.Advanced})
@AggregateOrder(71)
public class SchoolTopStudentRateAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(SchoolTopStudentRateAggregator.class);

    private static final String QUERY_SCORE = "" +
            "select (\n" +
            "\tselect b.score from \n" +
            "\t\t(select (@row := @row +1) num,s.* from score_project s,(select @row :=0) a ORDER BY score desc) b \n" +
            "\t\twhere b.num = (select floor(COUNT(1) * {{rate}}) from score_project)\n" +
            "\t) score\n";

    private static final String QUERY_LIST = "select * from score_project  where score > {{score}} or score = {{score}} order by score desc";


    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ReportConfigService reportConfigService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        projectDao.execute("truncate table school_top_student_rate");

        LOG.info("开始统计项目ID {} 的学生尖子生比例......", projectId);
        double rate = reportConfig.getTopStudentRate();
        Row row = projectDao.queryFirst(QUERY_SCORE.replace("{{rate}}", String.valueOf(rate)));
        String score = row.getString("score");
        List<Row> provinceTopStudents = projectDao.query(QUERY_LIST.replace("{{score}}", score));

        aggregateSchoolTopStudent(projectId, projectDao, provinceTopStudents);
    }

    private void aggregateSchoolTopStudent(String projectId, DAO projectDao, List<Row> provinceTopStudents) {

        List<Row> students = projectDao.query("select * from student");
        int totalCount = provinceTopStudents.size();
        List<ProjectSchool> schools = schoolService.listSchool(projectId);

        List<Map<String, Object>> insertMap = new ArrayList<>();
        schools.forEach(school -> {
            String schoolId = school.getId();

            List<String> schoolStudents = students.stream()
                    .filter(row -> schoolId.equals(row.getString("school_id")))
                    .map(row -> row.getString("id"))
                    .collect(Collectors.toList());

            List<Row> schoolStudentScore = provinceTopStudents.stream()
                    .filter(row -> schoolStudents.contains(row.getString("student_id")))
                    .collect(Collectors.toList());

            insertMap.add(createMap(schoolId, totalCount, schoolStudentScore));
        });
        projectDao.insert(insertMap, "school_top_student_rate");

    }

    private Map<String, Object> createMap(String schoolId, int totalCount, List<Row> schoolStudentScore) {
        Map<String, Object> map = new HashMap<>();
        int count = schoolStudentScore.isEmpty() ? 0 : schoolStudentScore.size();
        map.put("school_id", schoolId);
        map.put("count", count);
        map.put("rate", (count * 1.0 / totalCount));
        return map;
    }

}

