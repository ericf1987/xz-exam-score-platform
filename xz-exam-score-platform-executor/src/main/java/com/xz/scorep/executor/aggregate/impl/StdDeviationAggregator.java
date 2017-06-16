package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author by fengye on 2017/5/16.
 */
@AggregateTypes({AggregateType.Advanced})
@AggregateOrder(65)
@Component
public class StdDeviationAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(StdDeviationAggregator.class);

    private static final String INSERT_PROVINCE_DATA = "insert into std_deviation (range_type,range_id," +
            "target_type,target_id,std_deviation) select \"Province\" range_type,s.province range_id,\n" +
            "\"{{targetType}}\" target_type,\"{{targetId}}\" target_id,\n" +
            "STD(score.score) std_deviation from {{table}} score,student s\n" +
            "where s.id = score.student_id\n" +
            "GROUP BY s.province";

    private static final String INSERT_SCHOOL_DATA = "insert into std_deviation (range_type,range_id," +
            "target_type,target_id,std_deviation) select \"School\" range_type,s.school_id range_id,\n" +
            "\"{{targetType}}\" target_type,\"{{targetId}}\" target_id,\n" +
            "STD(score.score) std_deviation from {{table}} score,student s\n" +
            "where s.id = score.student_id\n" +
            "GROUP BY school_id";

    private static final String INSERT_CLASS_DATA = "insert into std_deviation (range_type,range_id," +
            "target_type,target_id,std_deviation) select \"Class\" range_type,s.class_id range_id,\n" +
            "\"{{targetType}}\" target_type,\"{{targetId}}\" target_id,\n" +
            "STD(score.score) std_deviation from {{table}} score,student s\n" +
            "where s.id = score.student_id\n" +
            "GROUP BY s.class_id";


    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        DAO projectDao = daoFactory.getProjectDao(projectId);

        projectDao.execute("truncate table std_deviation");

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        LOG.info("正在统计项目ID {} 标准差...", projectId);

        processProjectData(projectDao, projectId);

        processSubjectData(projectDao, projectId, examSubjects);

    }

    private void processSubjectData(DAO projectDao, String projectId, List<ExamSubject> subjects) {
        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            String table = "score_subject_" + subjectId;

            String province = INSERT_PROVINCE_DATA
                    .replace("{{targetType}}", Keys.Target.Subject.name())
                    .replace("{{targetId}}", subjectId)
                    .replace("{{table}}", table);

            projectDao.execute(province);
            String school = INSERT_SCHOOL_DATA
                    .replace("{{targetType}}", Keys.Target.Subject.name())
                    .replace("{{targetId}}", subjectId)
                    .replace("{{table}}", table);

            projectDao.execute(school);
            String clazz = INSERT_CLASS_DATA
                    .replace("{{targetType}}", Keys.Target.Subject.name())
                    .replace("{{targetId}}", subjectId)
                    .replace("{{table}}", table);
            projectDao.execute(clazz);
        });
    }

    private void processProjectData(DAO projectDao, String projectId) {
        String province = INSERT_PROVINCE_DATA
                .replace("{{targetType}}", Keys.Target.Project.name())
                .replace("{{targetId}}", projectId)
                .replace("{{table}}", "score_project");

        projectDao.execute(province);
        String school = INSERT_SCHOOL_DATA
                .replace("{{targetType}}", Keys.Target.Project.name())
                .replace("{{targetId}}", projectId)
                .replace("{{table}}", "score_project");

        projectDao.execute(school);
        String clazz = INSERT_CLASS_DATA
                .replace("{{targetType}}", Keys.Target.Project.name())
                .replace("{{targetId}}", projectId)
                .replace("{{table}}", "score_project");
        projectDao.execute(clazz);
    }


}
