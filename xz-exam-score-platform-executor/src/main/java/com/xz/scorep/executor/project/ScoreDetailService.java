package com.xz.scorep.executor.project;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScoreDetailService {

    private static String QUERY_STUDENT_PROJECT_SCORE = "select score.score,s.province rangeId\n" +
            "from {{tableName}} score\n" +
            "LEFT JOIN student s on s.id = score.student_id where score is not null";

    private static String QUERY_STUDENT_SCHOOL_SCORE = "select score.score,school.id rangeId \n" +
            "from school\n" +
            "LEFT JOIN student s on s.school_id = school.id \n" +
            "LEFT JOIN {{tableName}} score on score.student_id = s.id\n" +
            "where school.id = \"{{schoolId}}\" and score is not null ";

    private static String QUERY_STUDENT_CLASS_SCORE = "select score.score,class.id rangeId \n" +
            "from {{tableName}} score\n" +
            "LEFT JOIN student  on student.id = score.student_id\n" +
            "LEFT JOIN class on student.class_id = class.id\n" +
            "where class.id = \"{{classId}}\" and score is not null ";

    @Autowired
    private QuestService questService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private ClassService classService;

    @Autowired
    private DAOFactory daoFactory;

    public Map<String, Double> getStudentSubjectScoreDetail(String projectId, String studentId, String subjectId) {
        Map<String, Double> result = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        questService.queryQuests(projectId, subjectId).forEach(quest -> {
            String tableName = "score_" + quest.getId();
            Row row = projectDao.queryFirst("select score from `" + tableName + "` where student_id=?", studentId);
            if (row != null) {
                result.put(quest.getId(), row.getDouble("score", 0));
            }
        });

        return result;
    }

    //如果此处返回多个rang值,会有问题
    public Map<String, List<Row>> getStudentProjectScores(String projectId, String tableName) {
        Map<String, List<Row>> result = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> rows = projectDao.query(QUERY_STUDENT_PROJECT_SCORE.replace("{{tableName}}", tableName));
        result.put(rows.get(0).getString("rangeId"), rows);

        return result;
    }

    public Map<String, List<Row>> getStudentSchoolScores(String projectId, String tableName) {
        Map<String, List<Row>> result = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        schoolService.listSchool(projectId).forEach(school -> {
            String schoolId = school.getId();
            List<Row> rows = projectDao.query(QUERY_STUDENT_SCHOOL_SCORE
                    .replace("{{tableName}}", tableName)
                    .replace("{{schoolId}}", schoolId));
            result.put(schoolId, rows);
        });

        return result;
    }

    public Map<String, List<Row>> getStudentClassScores(String projectId, String tableName) {
        Map<String, List<Row>> result = new HashMap<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        classService.listClasses(projectId).forEach(clazz -> {
            String classId = clazz.getId();
            List<Row> rows = projectDao.query(QUERY_STUDENT_CLASS_SCORE
                    .replace("{{tableName}}", tableName)
                    .replace("{{classId}}", classId));
            result.put(classId, rows);
        });
        return result;
    }
}
