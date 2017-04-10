package com.xz.scorep.executor.aggritems;

import com.hyd.dao.Row;
import com.hyd.dao.database.commandbuilder.Command;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.db.DAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StudentQuery {

    private static final String SUBJECT_SCORE_RANK_TEMPLATE = "select \n" +
            "  s.id as student_id,\n" +
            "  ss.score as total_{{subject}},\n" +
            "  sbj.score as subjective_{{subject}},\n" +
            "  obj.score as objective_{{subject}},\n" +
            "  rc.`rank` as rank_class_{{subject}},\n" +
            "  rs.`rank` as rank_school_{{subject}},\n" +
            "  rp.`rank` as rank_province_{{subject}}\n" +
            "from \n" +
            "  student s, \n" +
            "  score_subject_{{subject}} ss, \n" +
            "  score_subjective_{{subject}} sbj,\n" +
            "  score_objective_{{subject}} obj,\n" +
            "  rank_class rc,\n" +
            "  rank_school rs,\n" +
            "  rank_province rp\n" +
            "where \n" +
            "  {{range}}" +
            "  s.id=ss.student_id AND\n" +
            "  s.id=sbj.student_id AND\n" +
            "  s.id=obj.student_id AND\n" +
            "  s.id=rc.student_id AND rc.subject_id='{{subject}}' AND\n" +
            "  s.id=rs.student_id AND rs.subject_id='{{subject}}' AND\n" +
            "  s.id=rp.student_id AND rp.subject_id='{{subject}}' ";

    @Autowired
    private DAOFactory daoFactory;

    public int getSchoolStudentCount(String projectId, String schoolId) {
        String sql = "select count(1) from student, class where student.class_id=class.id and class.school_id=?";
        return daoFactory.getProjectDao(projectId).count(sql, schoolId);
    }

    public Map<String, Integer> getClassStudentCount(String projectId, String schoolId) {
        String sql = "select count(1) as cnt, class.id as class_id " +
                "  from student, class " +
                "  where student.class_id=class.id and class.school_id=?" +
                "  group by class.id";

        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql, schoolId);
        return rows.stream().collect(Collectors.toMap(
                row -> row.getString("class_id"),
                row -> row.getInteger("cnt", 0)
        ));
    }

    // 查询考生基本信息
    public List<Row> listStudentInfo(String projectId, Range range) {

        String sql = "select " +
                "  student.id as student_id, " +
                "  student.exam_no as exam_no, " +
                "  student.school_exam_no as school_exam_no, " +
                "  student.name as student_name, " +
                "  class.name as class_name," +
                "  school.name as school_name," +
                "  school.area as area " +
                "from " +
                "  student, class, school " +
                "where " +
                "  student.class_id=class.id and" +
                "  class.school_id=school.id ";

        List<Object> params = new ArrayList<>();

        if (range.match(Range.SCHOOL)) {
            sql += " and school.id=?";
            params.add(range.getId());
        } else if (range.match(Range.CLASS)) {
            sql += " and class.id=?";
            params.add(range.getId());
        }

        return daoFactory.getProjectDao(projectId).query(new Command(sql, params));
    }

    public List<Row> listStudentSubjectInfo(String projectId, String subjectId, Range range) {

        String rangeCondition = buildRangeCondition(range, "s.{{type}}='{{id}}' AND");

        String query = SUBJECT_SCORE_RANK_TEMPLATE
                .replace("{{subject}}", subjectId)
                .replace("{{range}}", rangeCondition);

        return daoFactory.getProjectDao(projectId).query(query);
    }

    public List<Row> listStudentQuestScore(String projectId, String questId, Range range) {
        String tableName = "score_" + questId;
        String sql = "select student_id, objective_answer, score as `score_" + questId + "` " +
                "from `" + tableName + "` score, student s " +
                "where score.student_id=s.id AND " + buildRangeCondition(range, "s.{{type}}='{{id}}'");

        return daoFactory.getProjectDao(projectId).query(sql);
    }

    public List<String> getStudentList(String projectId, Range range) {
        String tmp = "select student.id as student_id from student\n" +
                "where student.{{type}} = '{{id}}'";
        String sql = buildRangeCondition(range, tmp);
        List<Row> rows = daoFactory.getProjectDao(projectId).query(sql);
        return rows.stream()
                .map(row -> row.getString("student_id"))
                .collect(Collectors.toList());
    }

    private String buildRangeCondition(Range range, String template) {
        String type = range.getType().toLowerCase() + "_id";
        String id = range.getId();

        if (range.match(Range.SCHOOL) || range.match(Range.CLASS)) {
            return template.replace("{{type}}", type).replace("{{id}}", id);
        } else {
            return "";
        }
    }


}
