package com.xz.scorep.executor.exportaggrdata.query;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.hyd.simplecache.utils.MD5;
import com.xz.scorep.executor.bean.*;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.exportaggrdata.bean.Average;
import com.xz.scorep.executor.project.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author by fengye on 2017/7/17.
 */
@Component
public class AverageScoreQuery {

    @Autowired
    DAOFactory daoFactory;

    @Autowired
    QuestService questService;

    @Autowired
    PointService pointService;

    @Autowired
    PointLevelService pointLevelService;

    @Autowired
    SubjectService subjectService;

    @Autowired
    SchoolService schoolService;

    @Autowired
    ClassService classService;

    private static final Logger LOG = LoggerFactory.getLogger(AverageScoreQuery.class);

    public static final String PROJECT_DATA = "select '{{rangeName}}' range_name,student.{{rangeId}} range_id, \n" +
            "'{{targetName}}' target_name,'{{targetId}}' target_id\n" +
            "from `score_project` score ,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY student.{{rangeId}}";


    public static final String SUBJECT_SUBJECTIVE_DATA = "select '{{range_name}}' range_name,student.{{range_id}} range_id,\n" +
            "'{{target_name}}' target_name,'{{subjectId}}' target_id,\n" +
            "avg(score) average from `{{table}}` score,student \n" +
            "where score.student_id = student.id\n" +
            "GROUP BY student.{{range_id}}";

    public static final String POINT_DATA_Group = "select '{{range_name}}' range_name,student.{{range_id}} range_id," +
            "'{{target_name}}' target_name,score.point_id target_id,\n" +
            "avg(score.total_score) average from `score_point` score,student \n" +
            "where score.student_id = student.id\n" +
            "and score.point_id = '{{point_id}}'\n" +
            "GROUP BY student.{{range_id}};";

    public static final String POINT_LEVEL = "SELECT score.point, score.level, AVG(score.total_score) average, \n" +
            "stu.{{rangeId}} range_id,'{{rangeName}}' range_name\n" +
            "FROM score_point_level score, student stu\n" +
            "WHERE score.student_id = stu.id  \n" +
            "GROUP BY score.point, score.level, stu.{{rangeId}};";

    public static final String SUBJECT_LEVEL = "select student.{{rangeId}} range_id,'{{rangeName}}' range_name ," +
            "avg(score.total_score) average,score.`subject`,score.`level`,'{{targetName}}' target_name\n" +
            "from score_subject_level score,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY \n" +
            "student.{{rangeId}},score.`subject`,score.`level`;";

    public static final String QUEST_DATA = "select avg(score.score) average,'{{rangeName}}' range_name," +
            "student.{{rangeId}} range_id,'{{targetName}}' target_name,'{{targetId}}' target_id\n" +
            "from `{{table}}` score,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY student.{{rangeId}}";

    public List<Average> queryData(String projectId) {
        //project,subject,subjectCombination,subjectObjective,point,pointLevel,subjectLevel,quest
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        LOG.info("开始查询 AverageScore  数据.....");

        List<Row> projectRows = queryProjectData(projectId);
        LOG.info("   projectRows  size ... {}", projectRows.size());

        List<Row> subjectRows = subjects.stream()
                .map(subject -> querySubjectData(projectId, subject))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   subjectRows  size ... {}", subjectRows.size());

        List<Row> objectiveRows = subjects.stream()
                .map(subject -> queryObjectiveData(projectId, subject))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   objectiveRows  size ... {}", objectiveRows.size());

        List<Row> pointRows = pointService.listPoints(projectId).parallelStream()
                .map(point -> queryPointData(projectId, point))
                .flatMap(x -> x.stream())
                .collect(Collectors.toList());
        LOG.info("   pointRows  size ... {}", pointRows.size());

        List<Row> pointLevelRows = queryPointLevelData(projectId);
        LOG.info("   pointLevelRows  size ... {}", pointLevelRows.size());

        List<Row> subjectLevelRows = querySubjectLevelData(projectId);
        LOG.info("   subjectLevelRows size ... {}", subjectLevelRows.size());

        List<Row> questRows = queryQuestData(projectId);
        LOG.info("   questRows  size ... {}", questRows.size());

        List<Row> result = addAll(projectRows, subjectRows, objectiveRows, pointRows, pointLevelRows, subjectLevelRows, questRows);
        List<Average> collect = result.stream().map(row -> pakObj(row, projectId)).collect(Collectors.toList());
        LOG.info("查询完成 AverageScore  共 {} 条数据......", collect.size());
        return collect;
    }

    private List<Row> queryProjectData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tem = PROJECT_DATA.replace("{{targetName}}", Target.PROJECT).replace("{{targetId}}", projectId);
        List<Row> classRows = projectDao.query(tem.replace("{{rangeName}}", Range.CLASS).replace("{{rangeId}}", "class_id"));
        List<Row> schoolRows = projectDao.query(tem.replace("{{rangeName}}", Range.SCHOOL).replace("{{rangeId}}", "school_id"));
        List<Row> provinceRows = projectDao.query(tem.replace("{{rangeName}}", Range.PROVINCE).replace("{{rangeId}}", "province"));
        List<Row> result = new ArrayList<>();
        result.addAll(classRows);
        result.addAll(schoolRows);
        result.addAll(provinceRows);
        return result;
    }

    private List<Row> querySubjectData(String projectId, ExamSubject subject) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String name = subject.getId().length() > 3 ? "subjectCombination" : "subject";
        String table = "score_subject_" + subject.getId();
        String tem = SUBJECT_SUBJECTIVE_DATA.replace("{{subjectId}}", subject.getId()).replace("{{target_name}}", name).replace("{{table}}", table);
        List<Row> classRows = projectDao.query(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id"));
        List<Row> schoolRows = projectDao.query(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id"));
        List<Row> provinceRows = projectDao.query(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province"));
        List<Row> result = new ArrayList<>();
        result.addAll(classRows);
        result.addAll(schoolRows);
        result.addAll(provinceRows);
        return result;
    }

    private List<Row> queryObjectiveData(String projectId, ExamSubject subject) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String subjectId = subject.getId();
        String subjectiveTable = "score_subjective_" + subjectId;
        String objectiveTable = "score_subjective_" + subjectId;
        Map<String, String> subMap = new HashMap<>();
        subMap.put("subject", subjectId);
        subMap.put("objective", "false");
        Map<String, String> objMap = new HashMap<>();
        objMap.put("subject", subjectId);
        objMap.put("objective", "true");

        String tem = SUBJECT_SUBJECTIVE_DATA.replace("{{subjectId}}", subjectId).replace("{{target_name}}", Target.SUBJECT_OBJECTIVE);
        List<Row> subjectiveClassRows = projectDao.query(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id").replace("{{table}}", subjectiveTable));
        List<Row> subjectiveSchoolRows = projectDao.query(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id").replace("{{table}}", subjectiveTable));
        List<Row> subjectiveProvinceRows = projectDao.query(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province").replace("{{table}}", subjectiveTable));
        subjectiveClassRows.forEach(row -> row.put("target_id", subMap));
        subjectiveSchoolRows.forEach(row -> row.put("target_id", subMap));
        subjectiveProvinceRows.forEach(row -> row.put("target_id", subMap));

        List<Row> objectiveClassRows = projectDao.query(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id").replace("{{table}}", objectiveTable));
        List<Row> objectiveSchoolRows = projectDao.query(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id").replace("{{table}}", subjectiveTable));
        List<Row> objectiveProvinceRows = projectDao.query(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province").replace("{{table}}", subjectiveTable));
        objectiveClassRows.forEach(row -> row.put("target_id", objMap));
        objectiveSchoolRows.forEach(row -> row.put("target_id", objMap));
        objectiveProvinceRows.forEach(row -> row.put("target_id", objMap));


        return addAll(subjectiveClassRows, subjectiveSchoolRows, subjectiveProvinceRows, objectiveClassRows, objectiveSchoolRows, objectiveProvinceRows);
    }

    private List<Row> queryPointData(String projectId, Point point) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tem = POINT_DATA_Group.replace("{{target_name}}", "point").replace("{{point_id}}", point.getPointId());

        List<Row> classRows = projectDao.query(tem.replace("{{range_name}}", Range.CLASS).replace("{{range_id}}", "class_id"));
        List<Row> schoolRows = projectDao.query(tem.replace("{{range_name}}", Range.SCHOOL).replace("{{range_id}}", "school_id"));
        List<Row> provinceRows = projectDao.query(tem.replace("{{range_name}}", Range.PROVINCE).replace("{{range_id}}", "province"));
        List<Row> result = new ArrayList<>();
        result.addAll(classRows);
        result.addAll(schoolRows);
        result.addAll(provinceRows);
        return result;

    }

    private List<Row> queryPointLevelData(String projectId) {
        List<Row> result = new ArrayList<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String provinceSql = POINT_LEVEL.replace("{{rangeName}}", Range.PROVINCE)
                .replace("{{rangeId}}", Range.PROVINCE);
        List<Row> provinceRows = projectDao.query(provinceSql);
        pointLevelConvertToResult(result, provinceRows);

        String schoolSql = POINT_LEVEL.replace("{{rangeName}}", Range.SCHOOL)
                .replace("{{rangeId}}", "school_id");
        List<Row> schoolRows = projectDao.query(schoolSql);
        pointLevelConvertToResult(result, schoolRows);

        String classSql = POINT_LEVEL.replace("{{rangeName}}", Range.CLASS)
                .replace("{{rangeId}}", "class_id");
        List<Row> classRows = projectDao.query(classSql);
        pointLevelConvertToResult(result, classRows);
        return result;
    }

    private List<Row> queryQuestData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String tmp = QUEST_DATA.replace("{{targetName}}", Target.SUBJECT);
        List<ExamQuest> quests = questService.queryQuests(projectId);
        List<Row> result = new ArrayList<>();
        quests.forEach(quest -> {
            String questId = quest.getId();
            String table = "score_" + questId;
            result.addAll(projectDao.query(tmp.replace("{{rangeName}}", Range.PROVINCE).replace("{{table}}", table)
                    .replace("{{rangeId}}", "province").replace("{{targetId}}", questId)));

            result.addAll(projectDao.query(tmp.replace("{{rangeName}}", Range.SCHOOL).replace("{{table}}", table)
                    .replace("{{rangeId}}", "school_id").replace("{{targetId}}", questId)));

            result.addAll(projectDao.query(tmp.replace("{{rangeName}}", Range.CLASS).replace("{{table}}", table)
                    .replace("{{rangeId}}", "class_id").replace("{{targetId}}", questId)));

        });

        return result;
    }

    private List<Row> querySubjectLevelData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> result = new ArrayList<>();
        String tmp = SUBJECT_LEVEL.replace("{{targetName}}", Target.SUBJECT_LEVEL);
        List<Row> classRows = projectDao.query(tmp.replace("{{rangeId}}", "class_id").replace("{{rangeName}}", Range.CLASS));
        List<Row> schoolRows = projectDao.query(tmp.replace("{{rangeId}}", "school_id").replace("{{rangeName}}", Range.SCHOOL));
        List<Row> provinceRows = projectDao.query(tmp.replace("{{rangeId}}", Range.PROVINCE).replace("{{rangeName}}", Range.PROVINCE));

        subjectLevelConvertToResult(result, classRows);
        subjectLevelConvertToResult(result, schoolRows);
        subjectLevelConvertToResult(result, provinceRows);

        return result;
    }

    private void subjectLevelConvertToResult(List<Row> result, List<Row> rows) {
        rows.forEach(row -> {
            Map<String, String> map = new HashMap<>();
            String targetName = row.getString("target_name");
            String rangeId = row.getString("range_id");
            String rangeName = row.getString("range_name");
            double average = row.getDouble("average", 0);
            map.put("level", row.getString("level"));
            map.put("subject", row.getString("subject"));
            result.add(createRow(average, rangeName, rangeId, targetName, map));
        });

    }

    private void pointLevelConvertToResult(List<Row> result, List<Row> rows) {
        rows.forEach(row -> {
            double average = row.getDouble("average", 0);
            String rangeId = row.getString("range_id");
            String rangeName = row.getString("range_name");
            String point = row.getString("point");
            String level = row.getString("level");
            PointLevel pointLevel = new PointLevel(point, level);
            result.add(createRow(average, rangeName, rangeId, Target.POINT_LEVEL, pointLevel));
        });
    }

    private Row createRow(double average, String rangeName, String rangeId, String targetName, Object targetId) {
        Row result = new Row();
        result.put("average", average);
        result.put("range_name", rangeName);
        result.put("range_id", rangeId);
        result.put("target_name", targetName);
        result.put("target_id", targetId);
        return result;

    }

    private List<Row> addAll(List<Row>... rows) {
        List<Row> result = new ArrayList<>(100000);
        for (List<Row> list : rows) {
            result.addAll(list);
        }
        result.removeIf(HashMap::isEmpty);
        return result;
    }

    private Average pakObj(Row row, String projectId) {
        Average average = new Average();
        average.setAverage(row.getDouble("average", 0));

        Range range = new Range();
        range.setId(row.getString("range_id"));
        range.setName(row.getString("range_name"));
        average.setRange(range);

        Target target = new Target();
        target.setName(row.getString("target_name"));
        target.setId(row.get("target_id"));
        average.setTarget(target);

        average.setProject(projectId);
        average.setMd5(MD5.digest(UUID.randomUUID().toString()));
        return average;
    }

}
