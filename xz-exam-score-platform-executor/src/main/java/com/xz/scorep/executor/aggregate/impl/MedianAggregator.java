package com.xz.scorep.executor.aggregate.impl;

import com.hyd.dao.DAO;
import com.hyd.dao.Row;
import com.xz.ajiaedu.common.concurrent.Executors;
import com.xz.ajiaedu.common.report.Keys;
import com.xz.scorep.executor.aggregate.*;
import com.xz.scorep.executor.bean.ExamSubject;
import com.xz.scorep.executor.db.DAOFactory;
import com.xz.scorep.executor.project.ClassService;
import com.xz.scorep.executor.project.SchoolService;
import com.xz.scorep.executor.project.SubjectService;
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

import static com.xz.ajiaedu.common.report.Keys.Range.Class;
import static com.xz.ajiaedu.common.report.Keys.Range.*;
import static com.xz.ajiaedu.common.report.Keys.Target.Project;

/**
 * 中位数(班级,学校,项目 的每个科目已经总分的中位数.)
 *
 * @author luckylo
 */
@Component
@AggregateTypes({AggregateType.Complete})
@AggragateOrder(71)
public class MedianAggregator extends Aggregator {

    private static Logger LOG = LoggerFactory.getLogger(MedianAggregator.class);

    //                mysql calculate median
//    SELECT avg(t1.score) as median_val FROM (
//    SELECT @rownum:=@rownum+1 as `row_number`, d.score
//    FROM score_project d,  (SELECT @rownum:=0) r
//    WHERE 1
//            -- put some where clause here\n"+
//    ORDER BY d.score
//) as t1,
//(
//    SELECT count(*) as total_rows
//    FROM score_project d
//    WHERE 1
//            -- put same where clause here\n"+
//            ) as t2
//    WHERE 1
//    AND t1.row_number in ( floor((total_rows+1)/2), floor((total_rows+2)/2) );

    private static final String QUERY_SUBJECT_SCORE = "select * from {{table}} order by score desc";

    @Autowired
    private DAOFactory daoFactory;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ClassService classService;

    @Autowired
    private SchoolService schoolService;

    @Override
    public void aggregate(AggregateParameter aggregateParameter) throws Exception {
        String projectId = aggregateParameter.getProjectId();
        DAO projectDao = daoFactory.getProjectDao(projectId);

        List<ExamSubject> subjects = subjectService.listSubjects(projectId);
        projectDao.execute("truncate table median");

        LOG.info("开始统计项目 ID {} 中位数....", projectId);
        ThreadPoolExecutor pool = Executors.newBlockingThreadPoolExecutor(10, 10, 1);
        aggregatorMedian(projectId, projectDao, subjects, pool);
        LOG.info("项目 ID {} 中位数统计完成....", projectId);

    }

    private void aggregatorMedian(String projectId, DAO projectDao, List<ExamSubject> subjects, ThreadPoolExecutor pool) {

        List<Row> studentRows = projectDao.query("select * from student");

        //统计总分维度的中位数
        pool.submit(() -> aggregatorProjectMedian(projectId, projectDao, studentRows));

        //统计科目维度的中位数
        subjects.forEach(subject ->
                pool.submit(() -> aggregatorSubjectMedian(projectId, projectDao, subject, studentRows)));
    }

    private void aggregatorProjectMedian(String projectId, DAO projectDao, List<Row> studentRows) {

        List<Map<String, Object>> insertMap = new ArrayList<>();

        List<Row> rows = projectDao.query("select * from score_project order by score desc");
        double provinceMedian = calculateMedian(rows);

        Map<String, Object> provinceMap = createMap(Province.name(), "430000", Project.name(), projectId, provinceMedian);
        insertMap.add(provinceMap);

        schoolService.listSchool(projectId)
                .forEach(school -> {
                    String schoolId = school.getId();
                    List<String> studentList = studentRows.stream()
                            .filter(row -> schoolId.equals(row.getString("school_id")))
                            .map(row -> row.getString("id"))
                            .collect(Collectors.toList());

                    List<Row> studentScoreRows = rows.stream()
                            .filter(row -> studentList.contains(row.getString("student_id")))
                            .collect(Collectors.toList());
                    double schoolMedian = calculateMedian(studentScoreRows);
                    Map<String, Object> schoolMap = createMap(School.name(), schoolId, Project.name(), projectId, schoolMedian);
                    insertMap.add(schoolMap);
                });

        classService.listClasses(projectId)
                .forEach(clazz -> {
                    String classId = clazz.getId();
                    List<String> studentList = studentRows.stream()
                            .filter(row -> classId.equals(row.getString("class_id")))
                            .map(row -> row.getString("id"))
                            .collect(Collectors.toList());

                    List<Row> studentScoreRows = rows.stream()
                            .filter(row -> studentList.contains(row.getString("student_id")))
                            .collect(Collectors.toList());

                    double classMedian = calculateMedian(studentScoreRows);
                    Map<String, Object> classMap = createMap(Class.name(), classId, Project.name(), projectId, classMedian);
                    insertMap.add(classMap);
                });
        projectDao.insert(insertMap, "median");
    }

    private void aggregatorSubjectMedian(String projectId, DAO projectDao, ExamSubject subject, List<Row> studentRows) {
        List<Map<String, Object>> insertMap = new ArrayList<>();
        String subjectId = subject.getId();
        List<Row> rows = projectDao.query(QUERY_SUBJECT_SCORE.replace("{{table}}", "score_subject_" + subjectId));

        double median = calculateMedian(rows);
        Map<String, Object> provinceMap = createMap(Province.name(), "430000", Keys.Target.Subject.name(), subjectId, median);
        insertMap.add(provinceMap);

        schoolService.listSchool(projectId)
                .forEach(school -> {
                    String schoolId = school.getId();
                    List<String> studentList = studentRows.stream()
                            .filter(row -> schoolId.equals(row.getString("school_id")))
                            .map(row -> row.getString("id"))
                            .collect(Collectors.toList());

                    List<Row> studentScoreRows = rows.stream()
                            .filter(row -> studentList.contains(row.getString("student_id")))
                            .collect(Collectors.toList());
                    double schoolMedian = calculateMedian(studentScoreRows);
                    Map<String, Object> schoolMap = createMap(School.name(), schoolId, Keys.Target.Subject.name(), subjectId, schoolMedian);
                    insertMap.add(schoolMap);
                });

        classService.listClasses(projectId)
                .forEach(clazz -> {
                    String classId = clazz.getId();
                    List<String> studentList = studentRows.stream()
                            .filter(row -> classId.equals(row.getString("class_id")))
                            .map(row -> row.getString("id"))
                            .collect(Collectors.toList());

                    List<Row> studentScoreRows = rows.stream()
                            .filter(row -> studentList.contains(row.getString("student_id")))
                            .collect(Collectors.toList());

                    double classMedian = calculateMedian(studentScoreRows);
                    Map<String, Object> classMap = createMap(Class.name(), classId, Keys.Target.Subject.name(), subjectId, classMedian);
                    insertMap.add(classMap);
                });

        projectDao.insert(insertMap, "median");
    }

    private double calculateMedian(List<Row> rows) {
        if (rows.isEmpty()) {
            return -1;
        }
        int mod = rows.size() % 2;
        if (mod == 0) {
            int middle = rows.size() / 2;
            double middleScore = rows.get(middle).getDouble("score", 0);
            double score = rows.get(middle + 1).getDouble("score", 0);
            return (middleScore + score) / 2;
        } else {
            int index = ((rows.size() + 1) / 2) + 1;
            return rows.get(index).getDouble("score", 0);
        }
    }

    private Map<String, Object> createMap(String rangeType, String rangeId, String targetType, String targetId, double median) {
        Map<String, Object> map = new HashMap<>();
        map.put("range_type", rangeType);
        map.put("range_id", rangeId);
        map.put("target_type", targetType);
        map.put("target_id", targetId);
        map.put("median", median);
        return map;
    }
}
