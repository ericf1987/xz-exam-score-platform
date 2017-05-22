package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 最高分  最低分,科目,总分，班级,学校,项目
 *
 * @author luckylo
 */
@Component
@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggragateOrder(54)
public class MaxMinScoreAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(MaxMinScoreAggregator.class);
    private static final String DROP_MAX_MIN_SCORE_TABLE = "drop table if exists max_min_score";

    private static final String CREATE_MAX_MIN_SCORE_TABLE = "create table max_min_score(" +
            "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
            "max_score decimal(5,2),min_score decimal(5,2))";

    private static final String CREATE_MAX_MIN_SCORE_INDEX = "create index idxmaxminscore on max_min_score(range_type,range_id,target_type,target_id)";

    public static final String INSERT_PROJECT_MAX_MIN_SCORE = "insert into max_min_score(range_type,range_id,target_type,target_id,max_score,min_score)" +
            " select '{{rangeType}}' range_type,'{{rangeId}}' range_id,'{{targetType}}'target_type,'{{targetId}}' target_id,\n" +
            " max(score.score) as max_score,min(score.score) as mim_score from {{table}} score";

    public static final String INSERT_SCHOOL_MAX_MIN_SCORE = "insert into max_min_score(range_type,range_id,target_type,target_id,max_score,min_score)" +
            " select '{{rangeType}}' range_type,s.school_id range_id,'{{targetType}}'target_type,'{{targetId}}' target_id," +
            " max(score.score) as max_score,min(score.score) as mim_score from {{table}} score" +
            " left join student s on score.student_id = s.id" +
            " group by s.school_id";

    public static final String INSERT_CLASS_MAX_MIN_SCORE = "insert into max_min_score(range_type,range_id,target_type,target_id,max_score,min_score)" +
            " select '{{rangeType}}' range_type,s.class_id range_id,'{{targetType}}'target_type,'{{targetId}}' target_id,\n" +
            " max(score.score) as max_score,min(score.score) as mim_score from {{table}} score" +
            " left join student s on score.student_id = s.id" +
            " group by s.class_id";

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        DAO projectDao = daoFactory.getProjectDao(projectId);

        initializeTable(projectDao);

        processProjectData(projectDao, projectId);

        processSubjectData(projectDao, projectId, subjects);

    }

    private void initializeTable(DAO projectDao) {
        SqlUtils.initialTable(projectDao, DROP_MAX_MIN_SCORE_TABLE, CREATE_MAX_MIN_SCORE_TABLE, CREATE_MAX_MIN_SCORE_INDEX);
    }

    private void processProjectData(DAO projectDao, String projectId) {
        String province = INSERT_PROJECT_MAX_MIN_SCORE.replace("{{rangeType}}", Keys.Range.Province.name())
                .replace("{{rangeId}}", Range.PROVINCE_RANGE.getId())
                .replace("{{targetType}}", Keys.Target.Project.name())
                .replace("{{targetId}}", projectId)
                .replace("{{table}}", "score_project");
        projectDao.execute(province);

        String school = INSERT_SCHOOL_MAX_MIN_SCORE.replace("{{rangeType}}", Keys.Range.School.name())
                .replace("{{targetType}}", Keys.Target.Project.name())
                .replace("{{targetId}}", projectId)
                .replace("{{table}}", "score_project");
        projectDao.execute(school);

        String clazz = INSERT_CLASS_MAX_MIN_SCORE.replace("{{rangeType}}", Keys.Range.Class.name())
                .replace("{{targetType}}", Keys.Target.Project.name())
                .replace("{{targetId}}", projectId)
                .replace("{{table}}", "score_project");
        projectDao.execute(clazz);
    }


    private void processSubjectData(DAO projectDao, String projectId, List<ExamSubject> subjects) {
        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            processSubjectData(projectDao, projectId, subjectId);
        });
    }

    private void processSubjectData(DAO projectDao, String projectId, String subjectId) {
        String tableName = "score_subject_" + subjectId;
        String province = INSERT_PROJECT_MAX_MIN_SCORE.replace("{{rangeType}}", Keys.Range.Province.name())
                .replace("{{rangeId}}", Range.PROVINCE_RANGE.getId())
                .replace("{{targetType}}", Keys.Target.Subject.name())
                .replace("{{targetId}}", subjectId)
                .replace("{{table}}", tableName);
        projectDao.execute(province);

        String school = INSERT_SCHOOL_MAX_MIN_SCORE.replace("{{rangeType}}", Keys.Range.School.name())
                .replace("{{targetType}}", Keys.Target.Subject.name())
                .replace("{{targetId}}", subjectId)
                .replace("{{table}}", tableName);
        projectDao.execute(school);

        String clazz = INSERT_CLASS_MAX_MIN_SCORE.replace("{{rangeType}}", Keys.Range.Class.name())
                .replace("{{targetType}}", Keys.Target.Subject.name())
                .replace("{{targetId}}", subjectId)
                .replace("{{table}}", tableName);
        projectDao.execute(clazz);
    }
}
