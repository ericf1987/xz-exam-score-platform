package com.xz.scorep.executor.aggritems;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 学生考试相关情况查询
 * (此处用到平均分,要确保平均分已统计完成)
 *
 * @author luckylo
 * @see com.xz.scorep.executor.aggregate.impl.AverageScoreAggregator
 */
@Component
public class StudentExamQuery {

    private static final String QUERY_STUDENT_SCORE = "" +
            "select sp.score total_score,`subject`.real_score subject_score,\n" +
            "`objective`.score objective_score,`subjective`.score subjective_score \n" +
            "from score_project sp ,score_subject_{{subjectId}} `subject`,\n" +
            "score_objective_{{subjectId}} `objective`, score_subjective_{{subjectId}} `subjective`,\n" +
            "student s\n" +
            "WHERE sp.student_id = s.id\n" +
            "and `subject`.student_id = s.id\n" +
            "and subjective.student_id = s.id\n" +
            "and `objective`.student_id = s.id\n" +
            "and s.id= \"{{studentId}}\"\n";

    private static final String QUERY_STUDENT_RANK = "" +
            "select rank_school.rank as school_rank,rank_class.rank as class_rank  \n" +
            "from  rank_class,rank_school\n" +
            "where\n" +
            "rank_class.student_id = rank_school.student_id\n" +
            "and\n" +
            "rank_class.subject_id = rank_school.subject_id\n" +
            "and rank_class.student_id = \"{{studentId}}\"\n" +
            "and rank_class.subject_id = \"{{subjectId}}\" ";

    private static final String QUERY_AVERAGE_SCORE = "" +
            "select * from average_score \n" +
            "WHERE\n" +
            "range_type = \"{{rangeType}}\"\n" +
            "and range_id = \"{{rangeId}}\"\n" +
            "and target_type = \"Subject\"\n" +
            "and target_id = \"{{subjectId}}\"";

    private static final String QUERY_STUDENT_SUBJECT_SCORE = "" +
            "select * from score_subject_{{subjectId}} where student_id = \'{{studentId}}\'";


    private static final String QUERY_STUDENT_INFO = "" +
            "select school.name school_name,class.name class_name,student.name student_name \n" +
            "from student,school,class\n" +
            "where student.class_id = class.id\n" +
            "and student.school_id = school.id\n" +
            "and student.id = '{{studentId}}'";

    private static final Logger LOG = LoggerFactory.getLogger(StudentExamQuery.class);

    @Autowired
    private DAOFactory daoFactory;

    public Map<String, Object> queryStudentScore(String projectId, String subjectId, String studentId) {
        String dataBaseName = projectId + "_" + subjectId + "_bak";
        DAO projectDao = daoFactory.getProjectDao(dataBaseName);
        String sql = QUERY_STUDENT_SCORE
                .replace("{{subjectId}}", subjectId)
                .replace("{{studentId}}", studentId);
        return projectDao.queryFirst(sql);
    }

    public Map<String, Object> queryStudentRank(String projectId, String subjectId, String studentId) {
        String dataBaseName = projectId + "_" + subjectId + "_bak";
        DAO projectDao = daoFactory.getProjectDao(dataBaseName);
        String sql = QUERY_STUDENT_RANK
                .replace("{{subjectId}}", subjectId)
                .replace("{{studentId}}", studentId);
        return projectDao.queryFirst(sql);
    }

    public Map<String, Object> queryStudentOverAverage(String projectId, String subjectId, String schoolId, String classId, String studentId) {
        Map<String, Object> result = new HashMap<>();
        String dataBaseName = projectId + "_" + subjectId + "_bak";
        DAO projectDao = daoFactory.getProjectDao(dataBaseName);

        String schoolAverage = QUERY_AVERAGE_SCORE
                .replace("{{rangeType}}", "School")
                .replace("{{rangeId}}", schoolId)
                .replace("{{subjectId}}", subjectId);
        String classAverage = QUERY_AVERAGE_SCORE
                .replace("{{rangeType}}", "Class")
                .replace("{{rangeId}}", classId)
                .replace("{{subjectId}}", subjectId);
        String subjectScore = QUERY_STUDENT_SUBJECT_SCORE
                .replace("{{subjectId}}", subjectId)
                .replace("{{studentId}}", studentId);

        double score = projectDao.queryFirst(subjectScore).getDouble("real_score", 0);
        double school_score = projectDao.queryFirst(schoolAverage).getDouble("average_score", 0);
        double class_score = projectDao.queryFirst(classAverage).getDouble("average_score", 0);

        result.put("schoolAverage", school_score);
        result.put("overSchoolAverage", new BigDecimal(String.valueOf(score)).subtract(new BigDecimal(String.valueOf(school_score))));

        result.put("classAverage", class_score);
        result.put("overClassAverage", new BigDecimal(String.valueOf(score)).subtract(new BigDecimal(String.valueOf(class_score))));

        return result;
    }

    //查询学生基本信息(从备份库中查询学生信息)
    public Row queryStudentInfo(String projectId, String studentId, String subjectId) {
        String dataBase = projectId + "_" + subjectId + "_bak";
        DAO projectDao = daoFactory.getProjectDao(dataBase);
        String replace = QUERY_STUDENT_INFO.replace("{{studentId}}", studentId);
        return projectDao.queryFirst(replace);
    }
}
