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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 尖子生统计
 *
 * @author luckylo
 * @createTime 2017-06-12.
 */
@Component
@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggregateOrder(71)
public class TopStudentAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(TopStudentAggregator.class);

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

        aggregateProvinceData(projectId, projectDao, reportConfig);

    }

    private void aggregateProvinceData(String projectId, DAO projectDao, ReportConfig reportConfig) {
        double rate = reportConfig.getTopStudentRate();
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        List<Row> studentList = projectDao.query("select * from student");
        List<Row> rows = projectDao.query("select * from score_project order by score desc");

        schools.forEach(school -> {
            String schoolId = school.getId();

            List<String> students = studentList.stream()
                    .filter(row -> schoolId.equals(row.getString("school_id")))
                    .map(row -> row.getString("id"))
                    .collect(Collectors.toList());

            List<Row> studentScore = rows.stream()
                    .filter(row -> students.contains(row.getString("student_id")))
                    .collect(Collectors.toList());


        });


    }
}
