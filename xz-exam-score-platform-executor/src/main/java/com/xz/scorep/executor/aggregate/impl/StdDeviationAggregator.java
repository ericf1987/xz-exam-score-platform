package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.aggritems.AverageQuery;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.StdDeviation;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.ScoreDetailService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xz.ajiaedu.common.report.Keys.Range;
import static com.xz.ajiaedu.common.report.Keys.Target;

/**
 * @author by fengye on 2017/5/16.
 */
@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggragateOrder(65)
@Component
public class StdDeviationAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(StdDeviationAggregator.class);

    public static final String DROP_DEVIATION_TABLE = "DROP TABLE IF EXISTS std_deviation";

    public static final String CREATE_DEVIATION_TABLE = "create table std_deviation(" +
            "range_type varchar(20),range_id VARCHAR(40),target_type VARCHAR(20),target_id VARCHAR(40)," +
            "std_Deviation decimal(5,2))";

    public static final String CREATE_DEVIATION_INDEX = "create index idxstd on std_deviation(range_type,range_id,target_type,target_id)";

    @Autowired
    AverageQuery averageQuery;

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ClassService classService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ScoreDetailService scoreDetailService;

    @Autowired
    SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        initializeTable(projectDao);

        LOG.info("项目 {} 正在统计标准差...", projectId);
        processProjectData(projectId, projectDao);

        ThreadPoolExecutor poolExecutor = Executors
                .newBlockingThreadPoolExecutor(10, 10, 1);


        processSubjectData(poolExecutor, projectId, projectDao, examSubjects);

    }

    private void processSubjectData(ThreadPoolExecutor poolExecutor, String projectId, DAO projectDao, List<ExamSubject> subjects) throws InterruptedException {

        subjects.forEach(subject -> processSubjectData(projectId, projectDao, subject));

    }

    private void initializeTable(DAO projectDao) {
        SqlUtils.initialTable(projectDao, DROP_DEVIATION_TABLE, CREATE_DEVIATION_TABLE, CREATE_DEVIATION_INDEX);

    }

    private void processProjectData(String projectId, DAO projectDao) {

        List<StdDeviation> stdDeviations = new ArrayList<>();

        List<Row> provinceRows = projectDao.query(AverageQuery.AVG_PROJECT_PROVINCE_GROUP.replace("{{table}}", "score_project"));
        Map<String, List<Row>> projectScores = scoreDetailService.getStudentProjectScores(projectId, "score_project");
        Map<String, Double> projectStdDeviation = AggregatorHelper.calculateRangeStdDeviation(provinceRows, projectScores);

        addStdDeviationList(stdDeviations, projectStdDeviation, Range.Province.name(), Target.Project.name(), projectId);

        List<Row> schoolRows = projectDao.query(AverageQuery.AVG_PROJECT_SCHOOL_GROUP.replace("{{table}}", "score_project"));
        Map<String, List<Row>> schoolScores = scoreDetailService.getStudentSchoolScores(projectId, "score_project");
        Map<String, Double> schoolStdDeviation = AggregatorHelper.calculateRangeStdDeviation(schoolRows, schoolScores);

        addStdDeviationList(stdDeviations, schoolStdDeviation, Range.School.name(), Target.Project.name(), projectId);

        List<Row> classRows = projectDao.query(AverageQuery.AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", "score_project"));
        Map<String, List<Row>> classScores = scoreDetailService.getStudentClassScores(projectId, "score_project");
        Map<String, Double> classStdDeviation = AggregatorHelper.calculateRangeStdDeviation(classRows, classScores);

        addStdDeviationList(stdDeviations, classStdDeviation, Range.Class.name(), Target.Project.name(), projectId);
        projectDao.insert(stdDeviations, "std_deviation");

        LOG.info("完成项目ID{}总分的标准差统计....,大小 ..{}", projectId, stdDeviations.size());

    }

    private void addStdDeviationList(List<StdDeviation> stdDeviations, Map<String, Double> projectStdDeviation,
                                     String rangType, String targetType, String targetId) {
        for (Map.Entry<String, Double> entry : projectStdDeviation.entrySet()) {
            String rangeId = entry.getKey();
            double stdDeviation = entry.getValue();
            stdDeviations.add(new StdDeviation(rangeId, rangType, targetId, targetType, stdDeviation));
        }

    }

    private void processSubjectData(String projectId, DAO projectDao, ExamSubject subject) {
        List<StdDeviation> stdDeviations = new ArrayList<>();
        String subjectId = subject.getId();
        String tableName = "score_subject_" + subjectId;

        List<Row> provinceRows = projectDao.query(AverageQuery.AVG_PROJECT_PROVINCE_GROUP.replace("{{table}}", tableName));
        Map<String, List<Row>> projectScores = scoreDetailService.getStudentProjectScores(projectId, tableName);
        Map<String, Double> projectStdDeviation = AggregatorHelper.calculateRangeStdDeviation(provinceRows, projectScores);

        addStdDeviationList(stdDeviations, projectStdDeviation, Range.Province.name(), Target.Subject.name(), subjectId);

        List<Row> schoolRows = projectDao.query(AverageQuery.AVG_PROJECT_SCHOOL_GROUP.replace("{{table}}", tableName));
        Map<String, List<Row>> schoolScores = scoreDetailService.getStudentSchoolScores(projectId, tableName);
        Map<String, Double> schoolStdDeviation = AggregatorHelper.calculateRangeStdDeviation(schoolRows, schoolScores);

        addStdDeviationList(stdDeviations, schoolStdDeviation, Range.School.name(), Target.Subject.name(), subjectId);

        List<Row> classRows = projectDao.query(AverageQuery.AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", tableName));
        Map<String, List<Row>> classScores = scoreDetailService.getStudentClassScores(projectId, tableName);
        Map<String, Double> classStdDeviation = AggregatorHelper.calculateRangeStdDeviation(classRows, classScores);

        addStdDeviationList(stdDeviations, classStdDeviation, Range.Class.name(), Target.Subject.name(), subjectId);
        projectDao.insert(stdDeviations, "std_deviation");

        LOG.info("完成项目{},科目{} 总分的标准差 ,大小为 ..{}", projectId, subjectId, stdDeviations.size());
    }
}
