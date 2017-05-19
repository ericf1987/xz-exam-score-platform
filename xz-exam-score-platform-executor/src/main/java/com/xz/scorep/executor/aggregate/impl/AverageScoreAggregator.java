package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.report.Keys.Range;
import com.xz.ajiaedu.common.report.Keys.Target;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.aggritems.AverageQuery;
import com.xz.scorep.executor.bean.AverageScore;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.DoubleUtils;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 平均分统计,(个人觉得,快速报表也有平均分,建议提前)
 *
 * @author luckylo
 */
@Component
@AggregateTypes({AggregateType.Advanced, AggregateType.Advanced})
@AggragateOrder(51)
public class AverageScoreAggregator extends Aggregator {

    private static final String DROP_AVERAGE_SCORE_TABLE = "drop table if exists average_score";

    private static final String CREATE_AVERAGE_SCORE_TABLE = "create table average_score(" +
            "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
            "average_score decimal(5,2))";

    private static final String CREATE_AVERAGE_SCORE_INDEX = "create index idxavgscore on average_score(range_type,range_id,target_type,target_id)";

    private static Logger LOG = LoggerFactory.getLogger(AverageScoreAggregator.class);
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private DAOFactory daoFactory;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);

        initializeTable(projectDao);

        addProjectData(projectDao, projectId);

        addSubjectData(projectDao, projectId, subjects);

    }

    private void initializeTable(DAO projectDao) {
        SqlUtils.initialTable(projectDao, DROP_AVERAGE_SCORE_TABLE, CREATE_AVERAGE_SCORE_TABLE, CREATE_AVERAGE_SCORE_INDEX);
    }

    private void addProjectData(DAO projectDao, String projectId) {
        List<AverageScore> averageScores = new ArrayList<>();

        List<Row> provinceRows = projectDao.query(AverageQuery.AVG_PROJECT_PROVINCE_GROUP.replace("{{table}}", "score_project"));
        List<Row> schoolRows = projectDao.query(AverageQuery.AVG_PROJECT_SCHOOL_GROUP.replace("{{table}}", "score_project"));
        List<Row> classRows = projectDao.query(AverageQuery.AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", "score_project"));

        convertToRows(averageScores, provinceRows, Range.Province.name(), Target.Project.name(), projectId);
        convertToRows(averageScores, schoolRows, Range.School.name(), Target.Project.name(), projectId);
        convertToRows(averageScores, classRows, Range.Class.name(), Target.Project.name(), projectId);

        projectDao.insert(averageScores, "average_score");
        LOG.info("项目 ID {} 总分平均分统计完成 ....", projectId);
    }

    private void convertToRows(List<AverageScore> averageScores, List<Row> rows, String rangType, String targetType, String projectId) {
        rows.forEach(row -> {
            String rangeId = row.getString("rangeId");
            double average = DoubleUtils.round(row.getDouble("average", 0));
            averageScores.add(new AverageScore(rangType, rangeId, projectId, targetType, average));
        });
    }

    private void addSubjectData(DAO projectDao, String projectId, List<ExamSubject> subjects) {
        subjects.forEach(subject -> {
            List<AverageScore> averageScores = new ArrayList<>();
            String subjectId = subject.getId();
            String tableName = "score_subject_" + subjectId;

            List<Row> provinceRows = projectDao.query(AverageQuery.AVG_PROJECT_PROVINCE_GROUP.replace("{{table}}", tableName));
            List<Row> schoolRows = projectDao.query(AverageQuery.AVG_PROJECT_SCHOOL_GROUP.replace("{{table}}", tableName));
            List<Row> classRows = projectDao.query(AverageQuery.AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", tableName));

            convertToRows(averageScores, provinceRows, Range.Province.name(), Target.Subject.name(), subjectId);
            convertToRows(averageScores, schoolRows, Range.School.name(), Target.Subject.name(), subjectId);
            convertToRows(averageScores, classRows, Range.Class.name(), Target.Subject.name(), subjectId);

            projectDao.insert(averageScores, "average_score");
            LOG.info("项目ID {} ,科目 ID {} 平均分统计完成...", projectId, subjectId);
        });
    }
}
