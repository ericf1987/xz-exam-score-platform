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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 平均分统计,(个人觉得,快速报表也有平均分,建议提前)
 * 必须在科目得分和总分统计之后
 *
 * @author luckylo
 */
@Component
@AggregateTypes(AggregateType.Basic)
@AggragateOrder(8)
public class AverageScoreAggregator extends Aggregator {

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

        LOG.info("开始统计项目ID {} 平均分..........", projectId);
        projectDao.execute("truncate table average_score");
        addProjectData(projectDao, projectId);
        addSubjectData(projectDao, projectId, subjects);
        LOG.info("项目ID {} 平均分统计完成.........", projectId);

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

        });
    }
}
