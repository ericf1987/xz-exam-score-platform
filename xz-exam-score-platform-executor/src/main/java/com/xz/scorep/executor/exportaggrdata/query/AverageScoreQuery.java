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

    public static final String SUBJECT_PROJECT_DATA = "select * from `{{table}}` score,student\n" +
            "where student.id  = score.student_id";
    public static final String POINT_DATA = "select * from `score_point` score,student\n" +
            "where score.student_id = student.id";

    public static final String POINT_LEVEL = "SELECT score.point, score.level, AVG(score.total_score) average, \n" +
            "stu.{{rangeId}} range_id,'{{rangeName}}' range_name\n" +
            "FROM score_point_level score, student stu\n" +
            "WHERE score.student_id = stu.id  \n" +
            "GROUP BY score.point, score.level, stu.{{rangeId}};";

    public static final String QUEST_DATA = "select avg(score.score) average,'{{rangeName}}' range_name," +
            "student.{{rangeId}} range_id,'{{targetName}}' target_name,'{{targetId}}' target_id\n" +
            "from `{{table}}` score,student\n" +
            "where score.student_id = student.id\n" +
            "GROUP BY student.{{rangeId}}";

    public List<Average> queryData(String projectId) {
        //project,subject,subjectCombination,subjectObjective,point,pointLevel,subjectLevel,quest
        LOG.info("开始导出平均分数据.....");
        List<Row> projectRows = processProjectData(projectId);//project
        List<Row> subjectRows = processSubjectData(projectId);//subject and subjectCombination
        List<Row> objectiveRows = processObjectiveData(projectId);// subjective  and objective
        List<Row> pointRows = processPointData(projectId);//point
        List<Row> pointLevelRows = processPointLevelData(projectId);// pointLevel
        List<Row> subjectLevelRows = processSubjectLevelData(projectId);// subjectLevel  TODO implements
        List<Row> questRows = processQuestData(projectId);// quest

        List<Row> result = addAll(projectRows, subjectRows, objectiveRows, pointRows,pointLevelRows,subjectLevelRows,questRows);
//        List<Row> result = addAll(questRows);
        List<Average> collect = result.stream().map(row -> pakObj(row, projectId)).collect(Collectors.toList());
        LOG.info("平均分数据导出完毕......");
        return collect;
    }

    private List<Row> processQuestData(String projectId) {
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

    private List<Row> processSubjectLevelData(String projectId) {
        return null;
    }

    private List<Row> processPointLevelData(String projectId) {
        List<Row> result = new ArrayList<>();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        String provinceSql = POINT_LEVEL.replace("{{rangeName}}", Range.PROVINCE)
                .replace("{{rangeId}}", Range.PROVINCE);
        List<Row> provinceRows = projectDao.query(provinceSql);
        convertToResult(result, provinceRows);

        String schoolSql = POINT_LEVEL.replace("{{rangeName}}", Range.SCHOOL)
                .replace("{{rangeId}}", "school_id");
        List<Row> schoolRows = projectDao.query(schoolSql);
        convertToResult(result, schoolRows);

        String classSql = POINT_LEVEL.replace("{{rangeName}}", Range.CLASS)
                .replace("{{rangeId}}", "class_id");
        List<Row> classRows = projectDao.query(classSql);
        convertToResult(result, classRows);
        return result;
    }

    private void convertToResult(List<Row> result, List<Row> rows) {
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

    private List<Row> processPointData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<Row> result = new ArrayList<>(100000);
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        List<ProjectClass> classes = classService.listClasses(projectId);
        List<Point> points = pointService.listPoints(projectId);
        List<Row> rows = projectDao.query(POINT_DATA);
        points.forEach(point -> {
            String pointId = point.getPointId();
            List<Row> pointIdRows = rows.stream()
                    .filter(row -> row.getString("point_id").equals(pointId))
                    .collect(Collectors.toList());

            if (pointIdRows.isEmpty()) {
                return;
            }

            double provinceAvg = pointIdRows.stream()
                    .mapToDouble(row -> row.getDouble("total_score", 0))
                    .average().getAsDouble();
            result.add(createRow(provinceAvg, Range.PROVINCE, Range.PROVINCE_RANGE.getId(), Target.POINT, pointId));

            schools.forEach(school -> {
                double schoolAvg = rows.stream()
                        .filter(row -> school.getId().equals(row.getString("school_id")))
                        .mapToDouble(row -> row.getDouble("total_score", 0))
                        .average().getAsDouble();
                result.add(createRow(schoolAvg, Range.SCHOOL, school.getId(), Target.POINT, pointId));
            });
            classes.forEach(clazz -> {
                double schoolAvg = rows.stream()
                        .filter(row -> clazz.getId().equals(row.getString("class_id")))
                        .mapToDouble(row -> row.getDouble("total_score", 0))
                        .average().getAsDouble();
                result.add(createRow(schoolAvg, Range.CLASS, clazz.getId(), Target.POINT, pointId));
            });

        });
        return result;
    }

    private List<Row> processObjectiveData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        List<ProjectClass> classes = classService.listClasses(projectId);
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        List<Row> result = new ArrayList<>();
        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            Map<String, String> subMap = new HashMap<>();
            subMap.put("subject", subjectId);
            subMap.put("objective", "false");
            Map<String, String> objMap = new HashMap<>();
            objMap.put("subject", subjectId);
            objMap.put("objective", "true");
            String subjective = "score_subjective_" + subjectId;
            String objective = "score_objective_" + subjectId;
            List<Row> subjectiveRows = projectDao.query(SUBJECT_PROJECT_DATA.replace("{{table}}", subjective));
            List<Row> objectiveRows = projectDao.query(SUBJECT_PROJECT_DATA.replace("{{table}}", objective));

            //  subjective  and objective  data
            processObjectiveAndSubjectiveData(schools, classes, result, subMap, subjectiveRows);
            processObjectiveAndSubjectiveData(schools, classes, result, objMap, objectiveRows);

        });
        return result;
    }

    private void processObjectiveAndSubjectiveData(List<ProjectSchool> schools, List<ProjectClass> classes, List<Row> result, Map<String, String> targetMap, List<Row> subjectiveRows) {
        double subProvinceAvg = subjectiveRows.stream()
                .mapToDouble(row -> row.getDouble("score", 0)).average().getAsDouble();
        result.add(createRow(subProvinceAvg, Range.PROVINCE, Range.PROVINCE_RANGE.getId(), Target.SUBJECT_OBJECTIVE, targetMap));

        schools.forEach(school -> {
            double subSchoolAvg = subjectiveRows.stream()
                    .filter(row -> school.getId().equals(row.getString("school_id")))
                    .mapToDouble(row -> row.getDouble("score", 0))
                    .average().getAsDouble();
            result.add(createRow(subSchoolAvg, Range.SCHOOL, school.getId(), Target.SUBJECT_OBJECTIVE, targetMap));
        });
        classes.forEach(clazz -> {
            double subClassAvg = subjectiveRows.stream()
                    .filter(row -> clazz.getId().equals(row.getString("class_id")))
                    .mapToDouble(row -> row.getDouble("score", 0))
                    .average().getAsDouble();
            result.add(createRow(subClassAvg, Range.CLASS, clazz.getId(), Target.SUBJECT_OBJECTIVE, targetMap));
        });
    }

    private List<Row> processSubjectData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        List<ProjectClass> classes = classService.listClasses(projectId);
        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        List<Row> result = new ArrayList<>();
        subjects.forEach(subject -> {
            String subjectId = subject.getId();
            String table = "score_subject_" + subjectId;
            String targetName = subjectId.length() > 3 ? Target.SUBJECT_COMBINATION : Target.SUBJECT;
            List<Row> rows = projectDao.query(SUBJECT_PROJECT_DATA.replace("{{table}}", table));
            double provinceAvg = rows.stream()
                    .mapToDouble(row -> row.getDouble("score", 0)).average().getAsDouble();
            result.add(createRow(provinceAvg, Range.PROVINCE, Range.PROVINCE_RANGE.getId(), targetName, subjectId));

            schools.forEach(school -> {
                double schoolAvg = rows.stream()
                        .filter(row -> school.getId().equals(row.getString("school_id")))
                        .mapToDouble(row -> row.getDouble("score", 0))
                        .average().getAsDouble();
                result.add(createRow(schoolAvg, Range.SCHOOL, school.getId(), targetName, subjectId));
            });
            classes.forEach(clazz -> {
                double classAvg = rows.stream()
                        .filter(row -> clazz.getId().equals(row.getString("class_id")))
                        .mapToDouble(row -> row.getDouble("score", 0))
                        .average().getAsDouble();
                result.add(createRow(classAvg, Range.CLASS, clazz.getId(), targetName, subjectId));
            });

        });
        return result;
    }

    private List<Row> processProjectData(String projectId) {
        DAO projectDao = daoFactory.getProjectDao(projectId);
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        List<ProjectClass> classes = classService.listClasses(projectId);
        List<Row> rows = projectDao.query(SUBJECT_PROJECT_DATA.replace("{{table}}", "score_project"));
        List<Row> result = new ArrayList<>();

        double provinceAvg = rows.stream()
                .mapToDouble(row -> row.getDouble("score", 0)).average().getAsDouble();
        result.add(createRow(provinceAvg, Range.PROVINCE, Range.PROVINCE_RANGE.getId(), Target.PROJECT, projectId));

        schools.forEach(school -> {
            double schoolAvg = rows.stream()
                    .filter(row -> school.getId().equals(row.getString("school_id")))
                    .mapToDouble(row -> row.getDouble("score", 0))
                    .average().getAsDouble();
            result.add(createRow(schoolAvg, Range.SCHOOL, school.getId(), Target.PROJECT, projectId));
        });

        classes.forEach(clazz -> {
            double classAvg = rows.stream()
                    .filter(row -> clazz.getId().equals(row.getString("class_id")))
                    .mapToDouble(row -> row.getDouble("score", 0))
                    .average().getAsDouble();
            result.add(createRow(classAvg, Range.CLASS, clazz.getId(), Target.PROJECT, projectId));
        });
        return result;
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
