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

    private static final String AVG_PROJECT_SCHOOL = "select \n" +
            "  AVG(score) as average \n" +
            "from \n" +
            "  {{table}} score, student, class\n" +
            "where\n" +
            "  score.student_id=student.id and \n" +
            "  student.class_id=class.id and \n" +
            "  class.school_id=?";

    private static final String AVG_PROJECT_CLASSES = "select \n" +
            "  AVG(score) as average, class.id as class_id \n" +
            "from \n" +
            "  {{table}} score, student, class\n" +
            "where\n" +
            "  score.student_id=student.id and \n" +
            "  student.class_id=class.id and \n" +
            "  class.school_id=?\n" +
            "group by class.id";

    private static final String AVG_SUBJECT_PROVINCE = "select " +
            "'{{subject}}' as subject, avg(score) as average from score_subject_{{subject}}";

    private static final String AVG_SUBJECT_SCHOOL = "select " +
            "'{{subject}}' as subject, avg(score) as average from score_subject_{{subject}} where student_id in(\n" +
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

        subjectService.querySubjectIds(projectId).forEach(subject -> {
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
    public Map<String, String> getSchoolSubjectAverages(String projectId, String schoolId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<String> subjectQueries = new ArrayList<>();

        subjectService.querySubjectIds(projectId).forEach(subject -> {
            String subjectId = subject.getId();
            subjectQueries.add(AVG_SUBJECT_SCHOOL.replace("{{subject}}", subjectId));
        });

        String finalQuery = String.join(" union ", subjectQueries);
        String[] params = new String[subjectQueries.size()];
        Arrays.fill(params, schoolId);

        List<Row> rows = projectDao.query(finalQuery, (Object[]) params);

        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("subject"),
                row -> row.getString("average")
        ));
    }

    // 学校总分平均分
    public double getSchoolProjectAverage(String projectId, String schoolId) {
        String sql = AVG_PROJECT_SCHOOL.replace("{{table}}", "score_project");
        Row row = daoFactory.getProjectDao(projectId).queryFirst(sql, schoolId);
        return row == null ? 0 : row.getDouble("average", 0);
    }

    // 各班总分平均分
    public Map<String, Double> getClassProjectAverages(String projectId, String schoolId) {
        String sql = AVG_PROJECT_CLASSES.replace("{{table}}", "score_project");
        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql, schoolId);
        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("class_id"),
                row -> row.getDouble("average", 0)
        ));
    }

}
