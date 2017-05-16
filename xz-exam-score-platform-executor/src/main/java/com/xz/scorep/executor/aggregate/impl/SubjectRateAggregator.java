package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.aggritems.AverageQuery;
import com.xz.scorep.executor.aggritems.ScoreQuery;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.SubjectRate;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.StudentService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.utils.DoubleUtils;
import com.xz.scorep.executor.utils.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 科目贡献度
 *
 * @author by fengye on 2017/5/15.
 */
@AggregateTypes({AggregateType.Advanced, AggregateType.Complete})
@AggragateOrder(54)
@Component
public class SubjectRateAggregator extends Aggregator {

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    @Autowired
    StudentService studentService;

    @Autowired
    ScoreQuery scoreQuery;

    @Autowired
    AverageQuery averageQuery;

    @Autowired
    SubjectService subjectService;

    @Autowired
    DAOFactory daoFactory;

    public static final String CREATE_SUBJECT_TABLE = "CREATE TABLE subject_rate_{{subjectId}} (range_id VARCHAR(40), range_type VARCHAR(16), subject_rate DECIMAL(6,4))";

    public static final String CREATE_SUBJECT_INDEX = "CREATE INDEX idxsbr_{{subjectId}} ON subject_rate_{{subjectId}}(range_id, range_type)";

    public static final String DROP_SUBJECT_TABLE = "DROP TABLE IF EXISTS subject_rate_{{subjectId}}";

    public static final String SUBJECT_RATE_TABLE = "subject_rate_{{subjectId}}";

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();

        List<ExamSubject> examSubjects = subjectService.listSubjects(projectId);

        DAO projectDao = daoFactory.getProjectDao(projectId);

        doInitial(projectDao, examSubjects);

        //目前而言，只统计班级以上的学科贡献度
        processNonStudentData(projectDao, examSubjects);
    }

    public void doInitial(DAO projectDao, List<ExamSubject> examSubjects) {
        for (ExamSubject subject : examSubjects) {
            SqlUtils.initialTable(subject.getId(), projectDao, DROP_SUBJECT_TABLE, CREATE_SUBJECT_TABLE, CREATE_SUBJECT_INDEX);
        }
    }

    private void processNonStudentData(DAO projectDao, List<ExamSubject> examSubjects) {
        for (ExamSubject subject : examSubjects) {
            processNonStudentSubjectRate(projectDao, subject);
        }
    }

    private void processNonStudentSubjectRate(DAO projectDao, ExamSubject subject) {
        //查询总体
        String provinceName = Range.PROVINCE;
        List<Row> p_rs = projectDao.query(SqlUtils.replaceSubjectId(AverageQuery.AVG_PROJECT_PROVINCE_GROUP, "{{table}}", "score_subject_" + subject.getId()));
        List<Row> p_rp = projectDao.query(SqlUtils.replaceSubjectId(AverageQuery.AVG_PROJECT_PROVINCE_GROUP, "{{table}}", "score_project"));
        List<SubjectRate> r1 = getSubjectRateRows(provinceName, p_rs, p_rp);

        //查询学校
        String schoolName = Range.SCHOOL;
        List<Row> s_rs = projectDao.query(SqlUtils.replaceSubjectId(AverageQuery.AVG_PROJECT_SCHOOL_GROUP, "{{table}}", "score_subject_" + subject.getId()));
        List<Row> s_rp = projectDao.query(SqlUtils.replaceSubjectId(AverageQuery.AVG_PROJECT_SCHOOL_GROUP, "{{table}}", "score_project"));
        List<SubjectRate> r2 = getSubjectRateRows(schoolName, s_rs, s_rp);

        //查询班级
        String clazzName = Range.CLASS;
        List<Row> c_rs = projectDao.query(SqlUtils.replaceSubjectId(AverageQuery.AVG_PROJECT_CLASSES_GROUP, "{{table}}", "score_subject_" + subject.getId()));
        List<Row> c_rp = projectDao.query(SqlUtils.replaceSubjectId(AverageQuery.AVG_PROJECT_CLASSES_GROUP, "{{table}}", "score_project"));
        List<SubjectRate> r3 = getSubjectRateRows(clazzName, c_rs, c_rp);

        List<SubjectRate> rows = new ArrayList<>();
        rows.addAll(r1);
        rows.addAll(r2);
        rows.addAll(r3);

        projectDao.insert(rows, SUBJECT_RATE_TABLE.replace("{{subjectId}}", subject.getId()));
    }

    private List<SubjectRate> getSubjectRateRows(String rangeName, List<Row> subjectRows, List<Row> projectRows) {
        List<String> rangeIds = subjectRows.stream().map(s -> s.getString("rangeId")).collect(Collectors.toList());

        List<SubjectRate> subjectRates = new ArrayList<>();
        for (String rangeId : rangeIds) {
            //单科平均分
            double s_aver = subjectRows.stream()
                    .filter(s -> rangeId.equals(s.getString("rangeId")))
                    .mapToDouble(s -> s.getDouble("average", 0)).sum();

            //全科平均分
            double p_aver = projectRows.stream()
                    .filter(s -> rangeId.equals(s.getString("rangeId")))
                    .mapToDouble(s -> s.getDouble("average", 0)).sum();

            double subjectRate = 0 == p_aver ? 0 : DoubleUtils.round(s_aver / p_aver, true);
            subjectRates.add(new SubjectRate(rangeId, rangeName, subjectRate));
        }
        return subjectRates;
    }
}
