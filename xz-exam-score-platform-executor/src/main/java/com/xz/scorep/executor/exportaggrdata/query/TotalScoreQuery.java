package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.cryption.MD5;
import com.xz.scorep.executor.bean.ExamQuest;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.bean.Range;
import com.xz.scorep.executor.bean.Target;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.QuestService;
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

    private static final String POINT_DATA = "select '{{rangeType}}' range_type,student.{{rangeId}} range_id,\n" +
            "'{{targetType}}' target_type,score.point_id target_id,SUM(score.total_score) total_score\n" +
            "from score_point score ,student\n" +
            "where student.id = score.student_id\n" +
            "GROUP BY student.{{rangeId}},score.point_id";

    private static final String STUDENT_POINT_DATA = "select 'student' range_type,student_id range_id," +
            "'point' target_type,point_id target_id,total_score from score_point";


    private static final String POINT_LEVEL_DATA = "select '{{rangeType}}' range_type,student.{{rangeId}} range_id," +
            "sum(score.total_score) total_score,\n" +
            "score.point,score.`level`,'{{targetType}}' target_type\n" +
            "from score_point_level score,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY \n" +
            "student.{{rangeId}},score.point,score.`level`";

    private static final String STUDENT_POINT_LEVEL_DATA = "select '{{rangeType}}' range_type, student_id range_id ,\n" +
            "point,`level`,'{{targetType}}' target_type,sum(total_score) total_score\n" +
            "from score_point_level\n" +
            "GROUP BY student_id,point,`level`";

    private static final String SUBJECT_LEVEL_DATA = "select '{{rangeType}}' range_type,student.{{rangeId}} range_id," +
            "sum(score.total_score) total_score,\n" +
            "score.`subject`,score.`level`,'{{targetType}}' target_type\n" +
            "from score_subject_level score,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY \n" +
            "student.{{rangeId}},score.`subject`,score.`level`;";

    private static final String STUDENT_SUBJECT_LEVEL_DATA = "select '{{rangeType}}' range_type,student_id range_id," +
            "sum(total_score) total_score,\n" +
            "`subject`,`level`,'{{targetType}}' target_type\n" +
            "from score_subject_level \n" +
            "GROUP BY student_id,`subject`,`level`";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private QuestService questService;


    private static final Logger LOG = LoggerFactory.getLogger(TotalScoreQuery.class);

    public List<Map<String, Object>> queryData(String projectId) {
        LOG.info("开始查询 TotalScore  数据 ....");
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        //project
        List<Row> projectRows = queryProjectData(projectId);
        LOG.info("   projectRows  size  is ... {}", projectRows.size());
        //subject and  subjectCombination
        List<Row> subjectRows = subjects
                .stream()
                .map(subject -> querySubjectData(projectId, subject))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   subjectRows  size  is ... {}", subjectRows.size());

        //subjective and objective
        List<Row> objectiveRows = subjects.stream()
                .map(subject -> queryObjectiveData(projectId, subject))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   objectiveRows  size ... {}", objectiveRows.size());

        //quest  range 为  class,school,province     (数量不对)
        List<Row> questRows = questService.queryQuests(projectId)
                .stream()
                .map(quest -> queryQuestData(projectId, quest))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   questRows  size ... {}", questRows.size());

        //point range 为 student ,class ,school ,province
        List<Row> pointRows = queryPointData(projectId);
        LOG.info("   pointRows  size ... {}", pointRows.size());

        //pointLevel range 为 student ,class ,school ,province
        List<Row> pointLevelRows = queryPointLevelRows(projectId);
        LOG.info("   pointLevelRows  size ... {}", pointLevelRows.size());

        //subjectLevel range 为 student ,class ,school ,province
        List<Row> subjectLevelRows = querySubjectLevelRows(projectId);
        LOG.info("   subjectLevelRows  size ... {}", subjectLevelRows.size());

        List<Row> collect = addAll(projectRows, subjectRows, objectiveRows, questRows, pointRows, pointLevelRows, subjectLevelRows);
//        List<Row> collect = addAll(questRows, pointRows);
        List<Map<String, Object>> result = collect.stream().map(row -> packObj(row, projectId)).collect(Collectors.toList());
        LOG.info("查询完成 TotalScore 共 {} 条.....", result.size());
        return result;
    }

    private List<Row> queryProjectData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        long count = subjectService.listSubjects(projectId)
                .stream()
                .filter(subject -> !Boolean.getBoolean(subject.getVirtualSubject()))
                .count();

        List<Row> provinceRows = projectDao.query(PROVINCE_DATA.replace("{{table}}", "score_project").replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId));
        List<Row> schoolRows = projectDao.query(SCHOOL_DATA.replace("{{table}}", "score_project").replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId));
        List<Row> classRows = projectDao.query(CLASS_DATA.replace("{{table}}", "score_project").replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId));
        List<Row> studentRows = projectDao.query(PROJECT_STUDENT_DATA.replace("{{targetType}}", Target.PROJECT).replace("{{targetId}}", projectId).replace("{{count}}", String.valueOf(count)));

        return addAll(provinceRows, schoolRows, classRows, studentRows);
    }

    private List<Row> querySubjectData(String projectId, ExamSubject subject) {
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

    private List<Row> queryObjectiveData(String projectId, ExamSubject subject) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String subjectId = subject.getId();
        String target = Target.SUBJECT_COMBINATION;
        String subjectiveTable = "score_subjective_" + subjectId;
        String objectiveTable = "score_subjective_" + subjectId;

        Map<String, String> subMap = new HashMap<>();
        subMap.put("subject", subjectId);
        subMap.put("objective", "false");
        Map<String, String> objMap = new HashMap<>();
        objMap.put("subject", subjectId);
        objMap.put("objective", "true");

        List<Row> subjectiveProvinceRows = projectDao.query(PROVINCE_DATA.replace("{{table}}", subjectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> subjectiveSchoolRows = projectDao.query(SCHOOL_DATA.replace("{{table}}", subjectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> subjectiveClassRows = projectDao.query(CLASS_DATA.replace("{{table}}", subjectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> subjectiveStudentRows = projectDao.query(SUBJECT_STUDENT_DATA.replace("{{table}}", subjectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));

        subjectiveProvinceRows.forEach(row -> removeAndPut(subMap, row));
        subjectiveSchoolRows.forEach(row -> removeAndPut(subMap, row));
        subjectiveClassRows.forEach(row -> removeAndPut(subMap, row));
        subjectiveStudentRows.forEach(row -> removeAndPut(subMap, row));

        List<Row> objectiveProvinceRows = projectDao.query(PROVINCE_DATA.replace("{{table}}", objectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> objectiveSchoolRows = projectDao.query(SCHOOL_DATA.replace("{{table}}", objectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> objectiveClassRows = projectDao.query(CLASS_DATA.replace("{{table}}", objectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));
        List<Row> objectiveStudentRows = projectDao.query(SUBJECT_STUDENT_DATA.replace("{{table}}", objectiveTable).replace("{{targetType}}", target).replace("{{targetId}}", subjectId));

        objectiveProvinceRows.forEach(row -> removeAndPut(objMap, row));
        objectiveSchoolRows.forEach(row -> removeAndPut(objMap, row));
        objectiveClassRows.forEach(row -> removeAndPut(objMap, row));
        objectiveStudentRows.forEach(row -> removeAndPut(objMap, row));

        return addAll(subjectiveProvinceRows, subjectiveSchoolRows, subjectiveClassRows, subjectiveStudentRows,
                objectiveProvinceRows, objectiveSchoolRows, objectiveClassRows, objectiveStudentRows);
    }

    private List<Row> queryQuestData(String projectId, ExamQuest quest) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String questId = quest.getId();
        String table = "score_" + questId;
        List<Row> provinceRows = projectDao.query(PROVINCE_DATA.replace("{{table}}", table).replace("{{targetType}}", Target.QUEST).replace("{{targetId}}", questId));
        List<Row> schoolRows = projectDao.query(SCHOOL_DATA.replace("{{table}}", table).replace("{{targetType}}", Target.QUEST).replace("{{targetId}}", questId));
        List<Row> classRows = projectDao.query(CLASS_DATA.replace("{{table}}", table).replace("{{targetType}}", Target.QUEST).replace("{{targetId}}", questId));
        return addAll(provinceRows, schoolRows, classRows);
    }

    private List<Row> queryPointData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tmp = POINT_DATA.replace("{{targetType}}", Target.POINT);

        List<Row> provinceRows = projectDao.query(tmp.replace("{{rangeType}}", Range.PROVINCE).replace("{{rangeId}}", Range.PROVINCE));
        List<Row> schoolRows = projectDao.query(tmp.replace("{{rangeType}}", Range.SCHOOL).replace("{{rangeId}}", "school_id"));
        List<Row> classRows = projectDao.query(tmp.replace("{{rangeType}}", Range.CLASS).replace("{{rangeId}}", "class_id"));
        List<Row> studentRows = projectDao.query(STUDENT_POINT_DATA);
        return addAll(provinceRows, schoolRows, classRows, studentRows);
    }

    private List<Row> queryPointLevelRows(String projectId) {
        List<Row> result = new ArrayList<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tmp = POINT_LEVEL_DATA.replace("{{targetType}}", Target.POINT_LEVEL);
        List<Row> provinceRows = projectDao.query(tmp.replace("{{rangeType}}", Range.PROVINCE).replace("{{rangeId}}", Range.PROVINCE));
        List<Row> schoolRows = projectDao.query(tmp.replace("{{rangeType}}", Range.SCHOOL).replace("{{rangeId}}", "school_id"));
        List<Row> classRows = projectDao.query(tmp.replace("{{rangeType}}", Range.CLASS).replace("{{rangeId}}", "class_id"));
        List<Row> studentRows = projectDao.query(STUDENT_POINT_LEVEL_DATA.replace("{{rangeType}}", Range.STUDENT).replace("targetType", Target.POINT_LEVEL));

        pointLevelConvertToResult(provinceRows, result);
        pointLevelConvertToResult(schoolRows, result);
        pointLevelConvertToResult(classRows, result);
        pointLevelConvertToResult(studentRows, result);

        return result;
    }

    private List<Row> querySubjectLevelRows(String projectId) {
        List<Row> result = new ArrayList<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tmp = SUBJECT_LEVEL_DATA.replace("{{targetType}}", Target.SUBJECT_LEVEL);
        List<Row> provinceRows = projectDao.query(tmp.replace("{{rangeType}}", Range.PROVINCE).replace("{{rangeId}}", Range.PROVINCE));
        List<Row> schoolRows = projectDao.query(tmp.replace("{{rangeType}}", Range.SCHOOL).replace("{{rangeId}}", "school_id"));
        List<Row> classRows = projectDao.query(tmp.replace("{{rangeType}}", Range.CLASS).replace("{{rangeId}}", "class_id"));
        List<Row> studentRows = projectDao.query(STUDENT_SUBJECT_LEVEL_DATA.replace("{{rangeType}}", Range.STUDENT).replace("{{targetType}}", Target.SUBJECT_LEVEL));

        subjectLevelConvertToResult(provinceRows, result);
        subjectLevelConvertToResult(schoolRows, result);
        subjectLevelConvertToResult(classRows, result);
        subjectLevelConvertToResult(studentRows, result);

        return result;
    }

    private List<Row> addAll(List<Row>... lists) {
        List<Row> result = new ArrayList<>(100000);
        for (List<Row> list : lists) {
            result.addAll(list);
        }
        result.removeIf(HashMap::isEmpty);
        return result;
    }

    private void removeAndPut(Map<String, String> subMap, Row row) {
        row.remove("isAbsent");
        row.put("target_id", subMap);
    }

    private void pointLevelConvertToResult(List<Row> rows, List<Row> result) {
        rows.forEach(row -> {
            Map<String, Object> targetId = new HashMap<>();

            String rangeId = row.getString("range_id");
            String rangeType = row.getString("rangeType");
            String targetType = row.getString("targetType");
            double totalScore = row.getDouble("total_score", 0);

            targetId.put("point", row.getString("point"));
            targetId.put("level", row.getString("level"));

            result.add(createRow(rangeType, rangeId, targetType, targetId, totalScore));
        });
    }

    private void subjectLevelConvertToResult(List<Row> rows, List<Row> result) {
        rows.forEach(row -> {
            Map<String, Object> targetId = new HashMap<>();
            String rangeId = row.getString("range_id");
            String rangeType = row.getString("range_type");
            String targetType = row.getString("target_type");
            double totalScore = row.getDouble("total_score", 0);

            targetId.put("subject", row.getString("subject"));
            targetId.put("level", row.getString("level"));

            result.add(createRow(rangeType, rangeId, targetType, targetId, totalScore));
        });
    }

    private Row createRow(String rangeType, String rangeId, String targetType, Object targetId, double totalScore) {
        Row row = new Row();
        row.put("range_id", rangeId);
        row.put("range_type", rangeType);
        row.put("targetType", targetType);
        row.put("total_score", totalScore);
        row.put("targetId", targetId);
        return row;
    }

    private Map<String, Object> packObj(Row row, String projectId) {
        Map<String, Object> result = new HashMap<>();
        Range range = new Range();
        range.setId(row.getString("range_id"));
        range.setName(row.getString("range_type"));

        Target target = new Target();
        target.setId(row.get("target_id"));
        target.setName(row.getString("target_type"));

        result.put("range", range);
        result.put("target", target);
        result.put("totalScore", row.getDouble("total_score", 0));

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
