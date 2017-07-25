package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luckylo
 * @createTime 2017-07-25.
 */
@Component
public class TotalScoreQuery {


    private static final String PROVINCE_DATA = "" +
            "select 'province' range_type,student.province range_id, " +
            "sum(score.score) total_score,'{{targetType}}' target_type,'{{targetId}}' target_id,student.province province\n" +
            "from `{{table}}` score,student " +
            "where score.student_id = student.id " +
            "group by student.province";

    private static final String SCHOOL_DATA = "" +
            "select 'school' range_type,student.school_id range_id,\n" +
            "sum(score.score) total_score,'{{targetType}}' target_type,'{{targetId}}' target_id,\n" +
            "student.school_id schoolId,school.area area,school.city city,school.province province \n" +
            "from `{{table}}` score,student,school\n" +
            "where score.student_id = student.id\n" +
            "and student.school_id = school.id \n" +
            "GROUP BY student.school_id";

    private static final String CLASS_DATA = "" +
            "select 'class' range_type,student.class_id range_id,\n" +
            "sum(score.score) total_score,'{{targetType}}' target_type,'{{targetId}}' target_id,\n" +
            "student.class_id classId,class.school_id schoolId,class.area,class.city,class.province \n" +
            "from `{{table}}` score,student,class\n" +
            "where score.student_id = student.id\n" +
            "and student.class_id = class.id \n" +
            "GROUP BY student.class_id";

    private static final String PROJECT_STUDENT_DATA = "" +
            "select 'student' range_type,student.id range_id, \n" +
            "ifnull(score.score,0) total_score,'{{targetType}}' target_type,'{{targetId}}' target_id,\n" +
            "student.school_id schoolId,student.class_id classId,\n" +
            "student.area area,student.city city,student.province province,\n" +
            "if(student.id in (\n" +
            "select a.student_id from \n" +
            "(select student_id,count(1) cnt from absent group by student_id) a \n" +
            "where a.cnt = {{count}}),\"true\",\"false\") isAbsent\n" +
            "from student\n" +
            "left join `score_project` score\n" +
            "on score.student_id = student.id ";

    private static final String SUBJECT_STUDENT_DATA = "" +
            "select 'student' range_type,student.id range_id, \n" +
            "ifnull(score.score,0) total_score,'{{targetType}}' target_type,'{{targetId}}' target_id,\n" +
            "student.school_id schoolId,student.class_id classId,\n" +
            "student.area area,student.city city,student.province province,\n" +
            "if(student.id in (select student_id from absent where subject_id = '{{targetId}}'),\"true\",\"false\") isAbsent\n" +
            "from student\n" +
            "left join `{{table}}` score\n" +
            "on score.student_id = student.id ";


    private static final Logger LOG = LoggerFactory.getLogger(TotalScoreQuery.class);

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    public List<Map<String, Object>> queryData(String projectId) {
        LOG.info("开始查询 TotalScore  数据 ....");
        List<Row> projectRows = processProjectData(projectId);
        LOG.info("   projectRows  size  is ... {}", projectRows.size());

        List<Row> subjectRows = subjectService.listSubjects(projectId)
                .stream()
                .map(subject -> processSubjectData(projectId, subject))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   subjectRows  size  is ... {}", subjectRows.size());


        List<Row> collect = addAll(projectRows, subjectRows);
        List<Map<String, Object>> result = collect.stream().map(row -> packObj(row, projectId)).collect(Collectors.toList());
        LOG.info("查询完成 TotalScore 共 {} 条.....", result.size());
        return result;
    }

    private List<Row> processSubjectData(String projectId, ExamSubject subject) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String subjectId = subject.getId();
        String table = "score_subject_" + subjectId;
        String target = subjectId.length() > 3 ? Target.SUBJECT_COMBINATION : Target.SUBJECT;
        List<Row> provinceRows = projectDao.query(PROVINCE_DATA.replace("{{table}}", table).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> schoolRows = projectDao.query(SCHOOL_DATA.replace("{{table}}", table).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> classRows = projectDao.query(CLASS_DATA.replace("{{table}}", table).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> studentRows = projectDao.query(SUBJECT_STUDENT_DATA.replace("{{table}}", table).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        return addAll(provinceRows, schoolRows, classRows, studentRows);
    }

    private List<Row> processProjectData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        long count = subjectService.listSubjects(projectId)
                .stream()
                .filter(subject -> !Boolean.getBoolean(subject.getVirtualSubject()))
                .count();
        List<Row> provinceRows = projectDao.query(PROVINCE_DATA.replace("{{table}}", "score_project").replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId));
        String school = SCHOOL_DATA.replace("{{table}}", "score_project").replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId);
        LOG.info("school sql  {}", school);
        List<Row> schoolRows = projectDao.query(school);
        String classSql = CLASS_DATA.replace("{{table}}", "score_project").replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId);
        LOG.info("class sql is {} ...", classSql);
        List<Row> classRows = projectDao.query(classSql);
        String replace = PROJECT_STUDENT_DATA.replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId).replace("{{count}}", String.valueOf(count));
        LOG.info("student sql is {} ...", replace);
        List<Row> studentRows = projectDao.query(replace);
        return addAll(provinceRows, schoolRows, classRows, studentRows);
    }

    private List<Row> addAll(List<Row>... lists) {
        List<Row> result = new ArrayList<>(100000);
        for (List<Row> list : lists) {
            result.addAll(list);
        }
        result.removeIf(HashMap::isEmpty);
        return result;
    }

    private Map<String, Object> packObj(Row row, String projectId) {
        Map<String, Object> result = new HashMap<>();
        Range range = new Range();
        range.setId(row.getString("range_id"));
        range.setName(row.getString("range_type"));

        Target target = new Target();
        target.setId(row.getString("target_id"));
        target.setName(row.getString("target_type"));

        result.put("range", range);
        result.put("target", target);
        result.put("totalScore", row.getDouble("totalScore", 0));

        //因查询维度不同,此部分数据可能不存在,可能部分存在
        //////////////////////////////////////////////////////////////////
        result.put("isAbsent", row.get("isAbsent"));
        result.put("school", row.getString("schoolId"));
        result.put("class", row.getString("classId"));
        result.put("area", row.getString("area"));
        result.put("city", row.getString("city"));
        result.put("province", row.getString("province"));
        //////////////////////////////////////////////////////////////////

        result.put("project", projectId);
        result.put("md5", MD5.digest(UUID.randomUUID().toString()));
        return result;
    }
}
