package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ProjectClass;
import com.xz.scorep.executor.bean.ProjectSchool;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
import com.xz.scorep.executor.reportconfig.ReportConfig;
import com.xz.scorep.executor.reportconfig.ReportConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 高分段统计(总体学校,学校班级)
 *
 * @author luckylo
 * @createTime 2017-06-21.
 */
@Component
@AggregateTypes({AggregateType.Advanced})
@AggregateOrder(72)
public class HighScoreAggregator extends Aggregator {

    private static final Logger LOG = LoggerFactory.getLogger(HighScoreAggregator.class);

    private static final String QUERY_PROVINCE_STUDENT = "select student_id from score_project\n" +
            "where score >=(\n" +
            "  select score from \n" +
            "  (\n" +
            "    select (@row :=@row+1) num,a.*  from (select * from score_project order by score desc) a,(select @row:=0) b \n" +
            "  ) c\n" +
            "  where num = (\n" +
            "  select floor(COUNT(1) * {{rate}}) from score_project)\n" +
            ")";
    public static final String SELECT_TARGET_DATA = "select avg(score) score from {{table}} where student_id in({{students}})";

    public static final String QUERY_SCHOOL_STUDENT = "select student_id from score_project score,student \n" +
            "where score.student_id = student.id\n" +
            "and student.school_id = \"{{schoolId}}\"\n" +
            "and score.score >=\n" +
            "(\n" +
            "  select score from \n" +
            "  (\n" +
            "    select (@row :=@row+1) num,a.* from \n" +
            "    (\n" +
            "      select score.student_id,score.score,student.school_id \n" +
            "      from score_project score,student\n" +
            "      where \n" +
            "      student.id = score.student_id \n" +
            "      and student.school_id = \"{{schoolId}}\"\n" +
            "      order by score.score desc\n" +
            "    ) a,(select @row :=0) b\n" +
            "  ) c\n" +
            "    where c.num = \n" +
            "  (\n" +
            "    select floor(COUNT(1) * {{rate}}) from score_project score,student \n" +
            "    where student.id = score.student_id\n" +
            "    and student.school_id = \"{{schoolId}}\"\n" +
            "  )\n" +
            ")";

    public static final String QUERY_CLASS_STUDENT = "select student_id from score_project score,student \n" +
            "where score.student_id = student.id\n" +
            "and student.class_id = \"{{classId}}\"\n" +
            "and score.score >=\n" +
            "(\n" +
            "  select score from \n" +
            "  (\n" +
            "    select (@row :=@row+1) num,a.* from \n" +
            "    (\n" +
            "      select score.student_id,score.score\n" +
            "      from score_project score,student\n" +
            "      where \n" +
            "      student.id = score.student_id \n" +
            "      and student.class_id = \"{{classId}}\"\n" +
            "      order by score.score desc\n" +
            "    ) a,(select @row :=0) b\n" +
            "  ) c\n" +
            "    where c.num = \n" +
            "  (\n" +
            "    select floor(COUNT(1) * {{rate}}) from score_project score,student \n" +
            "    where student.id = score.student_id\n" +
            "    and student.class_id = \"{{classId}}\"\n" +
            "  )\n" +
            ");";


    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private ReportConfigService reportConfigService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SubjectService subjectService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);
        ReportConfig reportConfig = reportConfigService.queryReportConfig(projectId);
        double rate = reportConfig.getHighScoreRate();

        projectDao.execute("truncate table high_score");
        LOG.info("项目ID {} 开始统计高分段学生....");
        ThreadPoolExecutor pool = Executors.newBlockingThreadPoolExecutor(20, 20, 1);

        aggregate0(projectId, projectDao, rate, pool);

        LOG.info("项目ID {} 高分段学生始统统计完成....");

    }

    private void aggregate0(String projectId, DAO projectDao, double rate, ThreadPoolExecutor pool) {
        pool.submit(() -> aggregateProvinceHigHScore(projectId, projectDao, rate));
        aggregateSchoolHighScore(projectId, projectDao, rate, pool);
        aggregateClassHighScore(projectId, projectDao, rate, pool);
    }

    private void aggregateProvinceHigHScore(String projectId, DAO projectDao, double rate) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        String replace = QUERY_PROVINCE_STUDENT.replace("{{rate}}", String.valueOf(rate));
        List<Row> studentRows = projectDao.query(replace);
        List<String> studentLists = studentRows.stream()
                .map(row -> "'" + row.getString("student_id") + "'")
                .collect(Collectors.toList());
        String students = String.join(",", studentLists);
        String tmp = SELECT_TARGET_DATA.replace("{{students}}", students);

        Row row = projectDao.queryFirst(tmp.replace("{{table}}", "score_project"));
        insertMap.add(createMap("Province", "430000", "Project", projectId, row.getDouble("score", 0)));

        subjectService.listSubjects(projectId)
                .forEach(subject -> {
                    String tableName = "score_subject_" + subject.getId();
                    Row subjectRow = projectDao.queryFirst(tmp.replace("{{table}}", tableName));
                    insertMap.add(createMap("Province", "430000", "Subject", subject.getId(), subjectRow.getDouble("score", 0)));
                });


        projectDao.insert(insertMap, "high_score");
    }

    private void aggregateSchoolHighScore(String projectId, DAO projectDao, double rate, ThreadPoolExecutor pool) {
        List<ProjectSchool> schools = schoolService.listSchool(projectId);
        for (ProjectSchool school : schools) {
            pool.submit(() -> processSchoolData(projectId, projectDao, rate, school));
        }
    }

    private void aggregateClassHighScore(String projectId, DAO projectDao, double rate, ThreadPoolExecutor pool) {
        List<ProjectClass> classes = classService.listClasses(projectId);
        for (ProjectClass clazz : classes) {
            pool.submit(() -> processClassData(projectId, projectDao, rate, clazz));
        }
    }

    private void processSchoolData(String projectId, DAO projectDao, double rate, ProjectSchool school) {
        String schoolId = school.getId();
        List<Map<String, Object>> insertMap = new ArrayList<>();
        String replace = QUERY_SCHOOL_STUDENT.replace("{{rate}}", String.valueOf(rate)).replace("{{schoolId}}", schoolId);
        List<Row> studentRows = projectDao.query(replace);
        List<String> stringList = studentRows.stream()
                .map(row -> "'" + row.getString("student_id") + "'")
                .collect(Collectors.toList());
        String students = String.join(",", stringList);

        String tmp = SELECT_TARGET_DATA.replace("{{students}}", students);

        Row row = projectDao.queryFirst(tmp.replace("{{table}}", "score_project"));
        insertMap.add(createMap("School", schoolId, "Project", projectId, row.getDouble("score", 0)));

        subjectService.listSubjects(projectId)
                .forEach(subject -> {
                    String tableName = "score_subject_" + subject.getId();
                    Row subjectRow = projectDao.queryFirst(tmp.replace("{{table}}", tableName));
                    insertMap.add(createMap("School", schoolId, "Subject", subject.getId(), subjectRow.getDouble("score", 0)));
                });


        projectDao.insert(insertMap, "high_score");
    }

    private void processClassData(String projectId, DAO projectDao, double rate, ProjectClass clazz) {
        String classId = clazz.getId();
        List<Map<String, Object>> insertMap = new ArrayList<>();
        String replace = QUERY_CLASS_STUDENT.replace("{{rate}}", String.valueOf(rate)).replace("{{classId}}", classId);
        List<Row> studentRows = projectDao.query(replace);
        List<String> stringList = studentRows.stream()
                .map(row -> "'" + row.getString("student_id") + "'")
                .collect(Collectors.toList());
        String students = String.join(",", stringList);

        String tmp = SELECT_TARGET_DATA.replace("{{students}}", students);

        Row row = projectDao.queryFirst(tmp.replace("{{table}}", "score_project"));
        insertMap.add(createMap("Class", classId, "Project", projectId, row.getDouble("score", 0)));

        subjectService.listSubjects(projectId)
                .forEach(subject -> {
                    String tableName = "score_subject_" + subject.getId();
                    Row subjectRow = projectDao.queryFirst(tmp.replace("{{table}}", tableName));
                    insertMap.add(createMap("Class", classId, "Subject", subject.getId(), subjectRow.getDouble("score", 0)));
                });


        projectDao.insert(insertMap, "high_score");
    }

    private Map<String, Object> createMap(String rangeType, String rangeId, String targetType, String targetId, double score) {
        Map<String, Object> map = new HashMap<>();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("target_type", targetType);
        map.put("target_id", targetId);
        map.put("score", score);
        return map;
    }

}
