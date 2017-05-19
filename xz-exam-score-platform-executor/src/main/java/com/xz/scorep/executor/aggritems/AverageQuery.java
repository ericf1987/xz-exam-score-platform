package com.xz.scorep.executor.aggritems;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AverageQuery {

    //总体平均分
    private static final String AVG_PROJECT_PROVINCE = "SELECT AVG(score) AS average FROM {{table}} score, student, class\n" +
            "WHERE student.class_id = class.id\n" +
            "AND class.province = ?";

    //总体平均分分组
    public static final String AVG_PROJECT_PROVINCE_GROUP = "SELECT AVG(score) AS average , student.province rangeId " +
            "FROM {{table}} score\n" +
            "left join student on score.student_id = student.id\n" +
            "GROUP BY rangeId";

    //学校平均分
    private static final String AVG_PROJECT_SCHOOL = "select \n" +
            "  AVG(score) as average \n" +
            "from \n" +
            "  {{table}} score, student, class\n" +
            "where\n" +
            "  score.student_id=student.id and \n" +
            "  student.class_id=class.id and \n" +
            "  class.school_id=?";

    //学校平均分分组
    public static final String AVG_PROJECT_SCHOOL_GROUP = "SELECT ifnull(AVG(score),0) AS average , school.id rangeId " +
            "FROM school\n" +
            "left join student on student.school_id = school.id\n" +
            "left join {{table}} score on score.student_id = student.id\n" +
            "GROUP BY rangeId;";

    //班级平均分
    private static final String AVG_PROJECT_CLASSES = "SELECT AVG(score) AS average " +
            "FROM {{table}} score, student, class\n" +
            "WHERE score.student_id = student.id\n" +
            "AND student.class_id = class.id\n" +
            "AND class.id = ?";

    //班级平均分分组
    public static final String AVG_PROJECT_CLASSES_GROUP = "SELECT ifnull(AVG(score),0) AS average , class.id rangeId " +
            "FROM  class\n" +
            "left join student on student.class_id = class.id\n" +
            "left join {{table}} score on score.student_id = student.id \n" +
            "GROUP BY rangeId;";

    private static final String AVG_SUBJECT_PROVINCE = "select " +
            "'{{subject}}' as subject, avg(score) as average from score_subject_{{subject}}";

    private static final String AVG_SUBJECT_SCHOOL = "select " +
            "'{{subjectId}}' as subject_id,'{{subjectName}}' as subject_name, avg(score) as avg_score from score_subject_{{subjectId}} where student_id in(\n" +
            "  select id from student where class_id in (\n" +
            "    select id from class where school_id=?\n" +
            "  )\n" +
            ")";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    // 全省各科平均分
    public Map<String, String> getProvinceSubjectAverages(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<String> subjectQueries = new ArrayList<>();

        subjectService.listSubjects(projectId).forEach(subject -> {
            String subjectId = subject.getId();
            subjectQueries.add(AVG_SUBJECT_PROVINCE.replace("{{subject}}", subjectId));
        });

        String finalQuery = String.join(" union ", subjectQueries);

        List<Row> rows = projectDao.query(finalQuery);

        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("subject"),
                row -> row.getString("average")
        ));
    }

    // 学校各科平均分
    public List<Row> getSchoolSubjectAverages(String projectId, String schoolId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<String> subjectQueries = new ArrayList<>();

        subjectService.listSubjects(projectId).forEach(subject -> {
            String subjectId = subject.getId();
            String subjectName = subject.getName();
            String sql = AVG_SUBJECT_SCHOOL
                    .replace("{{subjectId}}", subjectId).replace("{{subjectName}}", subjectName);
            subjectQueries.add(sql);
        });

        String finalQuery = String.join(" union ", subjectQueries);
        String[] params = new String[subjectQueries.size()];
        Arrays.fill(params, schoolId);

        return projectDao.query(finalQuery, (Object[]) params);

    }


    // 学校总分平均分
    public double getSchoolProjectAverage(String projectId, String schoolId) {
        String sql = AVG_PROJECT_SCHOOL.replace("{{table}}", "score_project");
        Row row = daoFactory.getProjectDao(projectId).queryFirst(sql, schoolId);
        return row == null ? 0 : row.getDouble("average", 0);
    }

    // 各班总分平均分
    public Map<String, Double> getClassProjectAverages(String projectId, String schoolId) {
        String sql = AVG_PROJECT_CLASSES_GROUP.replace("{{table}}", "score_project");
        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql, schoolId);
        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("class_id"),
                row -> row.getDouble("average", 0)
        ));
    }

}
