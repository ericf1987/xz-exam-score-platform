package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.aggritems.AverageQuery;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.DoubleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author by fengye on 2017/5/16.
 */
@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggragateOrder(63)
@Component
public class OverAverageAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(OverAverageAggregator.class);

    public static final String SUBJECT_TABLE_NAME = "over_average_{{subjectId}}";

    public static final String PROJECT_TABLE_NAME = "over_average_project";

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    ClassService classService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        projectDao.execute("truncate table over_average_project");

        LOG.info("正在统计项目ID {} 超均率", projectId);

        processProjectData(projectId, projectDao);

        processSubjectData(projectId, projectDao, examSubjects);
    }


    //处理考试项目数据
    private void processProjectData(String projectId, DAO projectDao) {
        List<Row> provinceRows = projectDao.query(AverageQuery.AVG_PROJECT_PROVINCE_GROUP.replace("{{table}}", "score_project"));
        List<Row> schoolRows = projectDao.query(AverageQuery.AVG_PROJECT_SCHOOL_GROUP.replace("{{table}}", "score_project"));
        List<Row> classRows = projectDao.query(AverageQuery.AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", "score_project"));

        List<OverAverage> schoolData = processSchoolData(projectId, Range.SCHOOL, Target.project(projectId), schoolRows, provinceRows);
        projectDao.insert(schoolData, PROJECT_TABLE_NAME);

        List<OverAverage> classData = processClassData(projectId, Range.CLASS, Target.project(projectId), classRows, schoolRows);
        projectDao.insert(classData, PROJECT_TABLE_NAME);

    }

    private void processSubjectData(String projectId, DAO projectDao, List<ExamSubject> examSubjects) {

        for (ExamSubject subject : examSubjects) {
            String subjectId = subject.getId();
            String sql = "truncate table over_average_{{subjectId}}";
            projectDao.execute(sql.replace("{{subjectId}}", subjectId));

            List<Row> provinceRows = projectDao.query(AverageQuery.AVG_PROJECT_PROVINCE_GROUP.replace("{{table}}", "score_subject_" + subjectId));
            List<Row> schoolRows = projectDao.query(AverageQuery.AVG_PROJECT_SCHOOL_GROUP.replace("{{table}}", "score_subject_" + subjectId));
            List<Row> classRows = projectDao.query(AverageQuery.AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", "score_subject_" + subjectId));

            List<OverAverage> schoolData = processSchoolData(projectId, Range.SCHOOL, Target.subject(subjectId), schoolRows, provinceRows);
            projectDao.insert(schoolData, SUBJECT_TABLE_NAME.replace("{{subjectId}}", subjectId));

            List<OverAverage> classData = processClassData(projectId, Range.CLASS, Target.subject(subjectId), classRows, schoolRows);
            projectDao.insert(classData, SUBJECT_TABLE_NAME.replace("{{subjectId}}", subjectId));
        }

    }

    private List<OverAverage> processSchoolData(String projectId, String school, Target target, List<Row> schoolRows, List<Row> provinceRows) {

        List<ProjectSchool> projectSchools = schoolService.listSchool(projectId);

        List<OverAverage> overAverages = new ArrayList<>();
        schoolRows.forEach(s_r -> {
            //学校平均分
            double school_average = s_r.getDouble("average", 0);
            //学校ID
            String rangeId = s_r.getString("rangeId");

            //获取当前学校所属的省份
            Optional<ProjectSchool> opt1 = projectSchools.stream().filter(p -> rangeId.equals(p.getId())).findFirst();
            String province_id = opt1.isPresent() ? opt1.get().getProvince() : "430000";

            //获取省份的平均分
            Optional<Row> opt2 = provinceRows.stream().filter(p -> province_id.equals(p.getString("rangeId"))).findFirst();
            double province_average = opt2.isPresent() ? opt2.get().getDouble("average", 0) : 0;

            overAverages.add(new OverAverage(rangeId, school, target.getId().toString(), target.getType(), DoubleUtils.round((school_average - province_average) / province_average, true)));
        });
        return overAverages;
    }

    private List<OverAverage> processClassData(String projectId, String aClass, Target target, List<Row> classRows, List<Row> schoolRows) {
        List<ProjectClass> projectClasses = classService.listClasses(projectId);

        List<OverAverage> overAverages = new ArrayList<>();
        classRows.forEach(s_r -> {
            //获取班级平均分
            double class_average = s_r.getDouble("average", 0);
            //获取班级ID
            String rangeId = s_r.getString("rangeId");

            //获取班级所在学校的ID
            Optional<ProjectClass> opt1 = projectClasses.stream().filter(p -> rangeId.equals(p.getId())).findFirst();
            String school_id = opt1.isPresent() ? opt1.get().getSchoolId() : "";

            //获取班级所在学校的平均分
            Optional<Row> opt2 = schoolRows.stream().filter(p -> school_id.equals(p.getString("rangeId"))).findFirst();
            double school_average = opt2.isPresent() ? opt2.get().getDouble("average", 0) : 0;

            overAverages.add(new OverAverage(rangeId, aClass, target.getId().toString(), target.getType(), DoubleUtils.round((class_average - school_average) / school_average, true)));
        });
        return overAverages;
    }

}
